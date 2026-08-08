-- sunfish.lua, a human transpiler work of https://github.com/thomasahle/sunfish
-- Code License: BSD

-- Localize global functions for massive performance gains in Luaj interpreter mode
local math_floor = math.floor
local math_abs = math.abs
local string_sub = string.sub
local string_byte = string.byte
local string_format = string.format

local NODES_SEARCHED = 1000
local MATE_VALUE = 30000
local TT_SIZE = 65536 -- fixed-size transposition table (bounded memory, ~64k slots)

-- Yield tuning. The search runs inside a coroutine (the Android RPD loop and
-- the test harness drive it) and yields periodically so the caller can poll.
-- Under LuaJ each coroutine.yield() is a JVM context hop (~330 switches per
-- 10k search at the old hardcoded 30-node quantum), so a bigger countdown
-- quantum is measurably faster (~33% at 256, ~41% at 1024; no-yield ~56%).
-- YIELD_QUANTUM is a tunable; the Android layer can lower it for
-- responsiveness or raise it for throughput. YIELD_ENABLED lets the
-- benchmark harness measure the uncapped ceiling (no coroutine switches).
local YIELD_QUANTUM = 256
local YIELD_ENABLED = true
-- Gate the per-depth search progress print. Under LuaJ (and Android log
-- routing) the unconditional print/string_format is expensive; SUNFISH_VERBOSE=1
-- enables it for debugging, otherwise search runs silent.
local VERBOSE = os.getenv("SUNFISH_VERBOSE") == "1"

-- E instrumentation: attacked() caller-class counters, active only when
-- SUNFISH_PROFILE_ATTACKED=1 (zero cost otherwise). Drives the plan's
-- decision on pin/check-aware legality; see benchmarks/profile_attacked.lua.
local PROF_ATTACKED = os.getenv("SUNFISH_PROFILE_ATTACKED") == "1"
local acnt_probe, acnt_king, acnt_touch, acnt_ep, acnt_castle = 0, 0, 0, 0, 0

local A1, H1, A8, H8 = 92, 99, 22, 29
local initial = '         \n' .. --   0 -  9
        '         \n' .. --  10 - 19
        ' rnbqkbnr\n' .. --  20 - 29
        ' pppppppp\n' .. --  30 - 39
        ' ........\n' .. --  40 - 49
        ' ........\n' .. --  50 - 59
        ' ........\n' .. --  60 - 69
        ' ........\n' .. --  70 - 79
        ' PPPPPPPP\n' .. --  80 - 89
        ' RNBQKBNR\n' .. --  90 - 99
        '         \n' .. -- 100 -109
        '          '     -- 110 -119

-------------------------------------------------------------------------------
-- Integer board representation
--
-- The engine's public API exposes `Position.board` as a 120-char string (as
-- documented and used by the tests), but under LuaJ every string_sub/byte is a
-- Java call. The hot path therefore uses a 120-element Lua array `_b` of
-- integer piece codes, and the string is materialized lazily only at public
-- API boundaries:
--
--   0     empty square ('.')
--   1..6  our pieces P,N,B,R,Q,K  (side to move, uppercase)
--  -1..-6 enemy pieces p,n,b,r,q,k (lowercase)
--   98    padding row '\n'
--   99    padding ' '
--
-- Indexing is 1-based (A1=92, H1=99, A8=22, H8=29, 1..120) since Phase 9
-- (full 1-based squares/constants/mirrors `121-x`); the public coordinate
-- helpers (parse/render/cell_2_move) convert at the boundary.
-------------------------------------------------------------------------------

-- Piece codes. NOTE: `N` (knight) is named KN to avoid colliding with the
-- direction constant N=-10 used throughout the engine.
local EMPTY, P, KN, B, R, Q, K = 0, 1, 2, 3, 4, 5, 6
local NL, SP = 98, 99

-- Packed move encoding. A move is a single integer instead of a {i, j, val}
-- table: value semantics (no aliasing), and genMoves allocates one number per
-- pseudo-legal move instead of one 3-cell table. Layout:
--   low 14 bits  : from-square i * 128 + to-square j   (i,j in 0..119)
--   high bits    : signed sort value, biased by VAL_BIAS (2^22)
-- Packed move layout (pure arithmetic; no bit32/bitwise ops, per the LuaJ
-- constraint):
--   bits 0-6   : to-square j            move_to:    v % 128
--   bits 7-13  : from-square i          move_from:  floor(v/128) % 128
--   bits 14-16 : promotion piece code   move_promo: floor(v/PROMO_UNIT) % 8
--                                          (0 = none; KN=2, B=3, R=4, Q=5)
--   bits 17+   : signed sort value      move_val:   floor(v/VAL_SCALE) - VAL_BIAS
local VAL_SHIFT = 17
local PROMO_SHIFT = 14
local VAL_BIAS = 2 ^ 22 -- half of the 23-bit value field
-- Precomputed constants (Lua 5.1 has no constant folding; `2 ^ VAL_SHIFT` and
-- `128 * 128` were recomputed on every call in the hot path).
local VAL_SCALE = 2 ^ VAL_SHIFT
local PROMO_UNIT = 2 ^ PROMO_SHIFT
local MOVE_MOD = VAL_SCALE -- low 17 bits hold i, j, promo (preserved on set_val)
-- move_pack(i, j, 0) — the zero-value packed move emitted by genMoves. Precomputed
-- so the hot movegen loop skips the (val + VAL_BIAS) * VAL_SCALE arithmetic.
local PACKED_ZERO_VAL = VAL_BIAS * VAL_SCALE
local function move_from(v)
    return math_floor(v / 128) % 128
end
local function move_to(v)
    return v % 128
end
local function move_promo(v)
    return math_floor(v / PROMO_UNIT) % 8
end
local function move_val(v)
    return math_floor(v / VAL_SCALE) - VAL_BIAS
end
-- Rewrite the value field of a packed move (sorting updates the cached value).
local function move_set_val(v, val)
    return (v % MOVE_MOD) + (val + VAL_BIAS) * VAL_SCALE
end

-- Emit one pawn move; if it reaches the last rank, expand into the 4 promotion
-- moves (underpromotion N/B/R/Q). Module-level (no closure) so genMoves does not
-- allocate per call. Returns the new move_idx.
local function emit_pawn(moves, move_idx, i, j)
    if A8 <= j and j <= H8 then
        local base = i * 128 + j + PACKED_ZERO_VAL
        moves[move_idx]     = base + KN * PROMO_UNIT -- knight
        moves[move_idx + 1] = base + B * PROMO_UNIT  -- bishop
        moves[move_idx + 2] = base + R * PROMO_UNIT  -- rook
        moves[move_idx + 3] = base + Q * PROMO_UNIT  -- queen
        return move_idx + 4
    end
    moves[move_idx] = i * 128 + j + PACKED_ZERO_VAL
    return move_idx + 1
end

-- ASCII byte -> piece code (for lazy string -> array conversion).
local byte_to_code = {}
for _i = 0, 255 do byte_to_code[_i] = EMPTY end
byte_to_code[string.byte('.')] = EMPTY
byte_to_code[string.byte('P')] = P
byte_to_code[string.byte('N')] = KN
byte_to_code[string.byte('B')] = B
byte_to_code[string.byte('R')] = R
byte_to_code[string.byte('Q')] = Q
byte_to_code[string.byte('K')] = K
byte_to_code[string.byte('p')] = -P
byte_to_code[string.byte('n')] = -KN
byte_to_code[string.byte('b')] = -B
byte_to_code[string.byte('r')] = -R
byte_to_code[string.byte('q')] = -Q
byte_to_code[string.byte('k')] = -K
byte_to_code[string.byte(' ')] = SP
byte_to_code[string.byte('\n')] = NL

-- Piece code -> single-char string (for array -> string materialization).
local code_to_char = {}
code_to_char[EMPTY] = '.'
code_to_char[P] = 'P'; code_to_char[-P] = 'p'
code_to_char[KN] = 'N'; code_to_char[-KN] = 'n'
code_to_char[B] = 'B'; code_to_char[-B] = 'b'
code_to_char[R] = 'R'; code_to_char[-R] = 'r'
code_to_char[Q] = 'Q'; code_to_char[-Q] = 'q'
code_to_char[K] = 'K'; code_to_char[-K] = 'k'
code_to_char[SP] = ' '
code_to_char[NL] = '\n'

-------------------------------------------------------------------------------
-- Move and evaluation tables
-------------------------------------------------------------------------------
local N, E, S, W = -10, 1, 10, -1
local directions = {
    P = { N, 2 * N, N + W, N + E },
    N = { 2 * N + E, N + 2 * E, S + 2 * E, 2 * S + E, 2 * S + W, S + 2 * W, N + 2 * W, 2 * N + W },
    B = { N + E, S + E, S + W, N + W },
    R = { N, E, S, W },
    Q = { N, E, S, W, N + E, S + E, S + W, N + W },
    K = { N, E, S, W, N + E, S + E, S + W, N + W }
}

local pst = {
    P = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 198, 198, 198, 198, 198, 198, 198, 198, 0,
          0, 178, 198, 198, 198, 198, 198, 198, 178, 0,
          0, 178, 198, 198, 198, 198, 198, 198, 178, 0,
          0, 178, 198, 208, 218, 218, 208, 198, 178, 0,
          0, 178, 198, 218, 238, 238, 218, 198, 178, 0,
          0, 178, 198, 208, 218, 218, 208, 198, 178, 0,
          0, 178, 198, 198, 198, 198, 198, 198, 178, 0,
          0, 198, 198, 198, 198, 198, 198, 198, 198, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    B = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 797, 824, 817, 808, 808, 817, 824, 797, 0,
          0, 814, 841, 834, 825, 825, 834, 841, 814, 0,
          0, 818, 845, 838, 829, 829, 838, 845, 818, 0,
          0, 824, 851, 844, 835, 835, 844, 851, 824, 0,
          0, 827, 854, 847, 838, 838, 847, 854, 827, 0,
          0, 826, 853, 846, 837, 837, 846, 853, 826, 0,
          0, 817, 844, 837, 828, 828, 837, 844, 817, 0,
          0, 792, 819, 812, 803, 803, 812, 819, 792, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    N = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 627, 762, 786, 798, 798, 786, 762, 627, 0,
          0, 763, 798, 822, 834, 834, 822, 798, 763, 0,
          0, 817, 852, 876, 888, 888, 876, 852, 817, 0,
          0, 797, 832, 856, 868, 868, 856, 832, 797, 0,
          0, 799, 834, 858, 870, 870, 858, 834, 799, 0,
          0, 758, 793, 817, 829, 829, 817, 793, 758, 0,
          0, 739, 774, 798, 810, 810, 798, 774, 739, 0,
          0, 683, 718, 742, 754, 754, 742, 718, 683, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    R = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 1258, 1263, 1268, 1272, 1272, 1268, 1263, 1258, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    Q = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 2529, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    K = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 60098, 60132, 60073, 60025, 60025, 60073, 60132, 60098, 0,
          0, 60119, 60153, 60094, 60046, 60046, 60094, 60153, 60119, 0,
          0, 60146, 60180, 60121, 60073, 60073, 60121, 60180, 60146, 0,
          0, 60173, 60207, 60148, 60100, 60100, 60148, 60207, 60173, 0,
          0, 60196, 60230, 60171, 60123, 60123, 60171, 60230, 60196, 0,
          0, 60224, 60258, 60199, 60151, 60151, 60199, 60258, 60224, 0,
          0, 60287, 60321, 60262, 60214, 60214, 60262, 60321, 60287, 0,
          0, 60298, 60332, 60273, 60225, 60225, 60273, 60332, 60298, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
}

-------------------------------------------------------------------------------
-- Chess logic
-------------------------------------------------------------------------------

-- Integer aliases so the hot path indexes by piece code (P=1..K=6).
directions[P] = directions['P']
directions[KN] = directions['N']
directions[B] = directions['B']
directions[R] = directions['R']
directions[Q] = directions['Q']
directions[K] = directions['K']
pst[P] = pst['P']
pst[KN] = pst['N']
pst[B] = pst['B']
pst[R] = pst['R']
pst[Q] = pst['Q']
pst[K] = pst['K']

-------------------------------------------------------------------------------
-- Precomputed attack/ray tables
--
-- The 0..119 index space is an 8x8 board padded to 10 columns: cols 0 and 9
-- of each row are spaces (SP) and rows 0/1 and 8/9 are padding, so a square
-- is "on the board" iff 20 <= i < 100 and i%10 is 1..8. Rays step until the
-- edge; the engine's while-loops stop at SP/NL, which is exactly this wall.
-------------------------------------------------------------------------------
local is_on_board = {}
local real_squares = {} -- the 64 real board squares, stored 1-based (built once)
for _i = 0, 119 do
    is_on_board[_i] = _i >= 20 and _i < 100 and (_i % 10) >= 1 and (_i % 10) <= 8
    if is_on_board[_i] then
        real_squares[#real_squares + 1] = _i + 1
    end
end

-- ray_squares[i][d]: squares along direction d (d in 1..8) from i.
-- knight_targets[i] / king_targets[i] / pawn_caps[i]: fixed move sets.
local ray_squares, knight_targets, king_targets, pawn_caps = {}, {}, {}, {}
local all_dirs = { N, E, S, W, N + E, S + E, S + W, N + W }
local knight_offsets = { 2 * N + E, N + 2 * E, S + 2 * E, 2 * S + E, 2 * S + W, S + 2 * W, N + 2 * W, 2 * N + W }
local king_offsets = { N, E, S, W, N + E, S + E, S + W, N + W }
for i = 0, 119 do
    if is_on_board[i] then
        local t = i + 1
        ray_squares[t] = {}
        for di, d in ipairs(all_dirs) do
            local j = i + d
            local sqs = {}
            local n = 0
            while is_on_board[j] do
                n = n + 1
                sqs[n] = j + 1
                j = j + d
            end
            n = n + 1
            sqs[n] = 0 -- sentinel terminator
            ray_squares[t][di] = sqs
        end
        local kt, kg, pc = {}, {}, {}
        local n
        for oi, o in ipairs(knight_offsets) do
            local j = i + o
            if is_on_board[j] then kt[#kt + 1] = j + 1 end
        end
        for oi, o in ipairs(king_offsets) do
            local j = i + o
            if is_on_board[j] then kg[#kg + 1] = j + 1 end
        end
        for _, o in ipairs({ N + W, N + E }) do
            local j = i + o
            if is_on_board[j] then pc[#pc + 1] = j + 1 end
        end
        -- Sentinel-pad the fixed-size target lists to their max lengths
        -- (knight 8, king 8, pawn 2). The consumers loop `for c = 1, MAX`
        -- and `break` on a 0 sentinel, so no `#` is evaluated in the hot
        -- loops (see genMoves/attacked).
        for n = #kt + 1, 8 do kt[n] = 0 end
        for n = #kg + 1, 8 do kg[n] = 0 end
        for n = #pc + 1, 2 do pc[n] = 0 end
        knight_targets[t] = kt
        king_targets[t] = kg
        pawn_caps[t] = pc
    end
end

-- direction index for each piece: P=1 pawn, KN=2 knight, B=3, R=4, Q=5, K=6
-- pawn: di 1 (N), 2 (2N), 3 (N+W), 4 (N+E) -- we handle pawns specially in genMoves
-- knight: 8 fixed targets
-- sliders: rook di 1..4, bishop di 5..8, queen di 1..8
local slider_dirs_by_piece = {
    [R] = { 1, 2, 3, 4 },
    [B] = { 5, 6, 7, 8 },
    [Q] = { 1, 2, 3, 4, 5, 6, 7, 8 }
}

-- on_ray[a*121 + b] = di when square b lies on a direction-di ray from square a
-- (a,b are 1-based squares), else 0. Built once from ray_squares so a pin/check
-- test is a single table read: a piece is pinned to the king iff on_ray[king*121+sq]
-- is nonzero and the enemy slider is beyond it on that same ray. Used by
-- compute_check_pins and the pin-aware is_legal.
local on_ray = {}
for a = 1, 120 do
    local base = a * 121
    local rs = ray_squares[a]
    if rs then
        for di = 1, 8 do
            local ray = rs[di]
            for c = 1, #ray do
                local sq = ray[c]
                if sq == 0 then break end
                on_ray[base + sq] = di
            end
        end
    end
end

-------------------------------------------------------------------------------
-- Transposition-table key (integer, cached per Position)
--
-- Pure Lua 5.1 (no bit32 on Android/LuaJ), so we use a Zobrist-style sum of
-- precomputed pseudo-random values. XOR would be ideal, but sum works fine for
-- a 64k-slot table: the stored full key is verified on every probe, so a
-- collision only costs a missed lookup, never a wrong result.
-- The key covers board + castling rights + ep + kp (NOT score: two move orders
-- reaching the same position must share a TT entry).
-------------------------------------------------------------------------------
local zob = {}
local zflat = {}
local zmirror = {}
local zob_wc = { 0, 0 }
local zob_bc = { 0, 0 }
local zob_ep = {}
local zob_kp = {}
do
    -- Deterministic Multiply-With-Carry PRNG. Pure Lua 5.1 arithmetic (no
    -- bit32/bitwise on Android/LuaJ). MWC gives well-distributed low bits,
    -- which matters because the TT slot is key % TT_SIZE (low bits of key).
    -- A bad low-bit PRNG (e.g. LCG) collapses the table into a few slots.
    local x = 88172645463325252 % 65536
    local c = math.floor(88172645463325252 / 65536) % 65536
    local function rnd()
        local t = x * 65539 + c
        c = math.floor(t / 65536)
        x = t % 65536
        return x + c * 65536
    end
    for pc = -6, 6 do
        zob[pc] = {}
        for sq = 0, 119 do
            zob[pc][sq] = rnd()
        end
    end
    -- Flat 1D alias for the hash loop: zflat[(pc+6)*120 + sq] == zob[pc][sq].
    -- A single table get instead of two saves a LuaJ Java call per square.
    zflat = {}
    for pc = -6, 6 do
        for sq = 0, 119 do
            zflat[(pc + 6) * 120 + sq + 1] = zob[pc][sq]
        end
    end
    -- Mirror-hash table for the dual-hash incremental Zobrist (item 2):
    --   zmirror[(pc+6)*120 + sq] == zflat[(-pc+6)*120 + (121-sq)]
    -- i.e. the contribution that a piece `pc` at 1-based square `sq` makes to
    -- the hash of a position obtained by rotating this one (mirror `121-sq` +
    -- negate `-pc`). The child square `121-sq` is already 1-based, so zflat's
    -- index is (pc+6)*120 + (121-sq) with NO extra +1. With this, the board
    -- hash of a rotated child is the parent's `_mh` (maintained alongside
    -- `_bh`), giving an O(1) rotate.
    zmirror = {}
    for pc = -6, 6 do
        for sq = 1, 120 do
            zmirror[(pc + 6) * 120 + sq] = zflat[(-pc + 6) * 120 + (121 - sq)]
        end
    end
    zob_wc[1], zob_wc[2] = rnd(), rnd()
    zob_bc[1], zob_bc[2] = rnd(), rnd()
    for sq = 0, 119 do
        zob_ep[sq + 1] = rnd()
    end
    zob_ep[121] = 0 -- mirror of the no-ep sentinel (121 - 0)
    zob_kp[1] = 0
    for sq = 1, 119 do
        zob_kp[sq + 1] = rnd()
    end
    zob_kp[121] = 0 -- mirror of the no-kp sentinel (121 - 0)
end

-- The old string-keyed maps (is_upper_map/is_lower_map/swap_map) are replaced
-- by the integer codes: p >= 1 means our piece, p < 0 means enemy, -p is the
-- enemy's piece type.

local Position = {}
Position.__index = Position -- Using a Metatable is ~5x faster in luaj than loop-copying methods!

-- Public constructor: accepts a string board (backward compatible).
function Position.new(board, score, wc, bc, ep, kp)
    local self = setmetatable({}, Position)
    self.board = board
    self.score = score
    self.wc = wc
    self.bc = bc
    self.ep = ep
    self.kp = kp
    return self
end

-- Internal constructor: position with an integer array board (_b).
-- Optional trailing args thread the cached king indices and dual-hashes
-- through search:
--   _king  = index of the side-to-move's king (code K)
--   _eking = index of the enemy king (code -K)
--   _bh    = board hash (sum of zflat over pieces in the current frame)
--   _mh    = mirror hash (sum of zmirror over pieces; == what _bh becomes
--            after a pure rotation)
--   _fh    = flag hash (wc/bc/ep/kp terms)
-- These avoid the up-to-120-square scans/hash passes on every fresh search
-- position.
function Position.from_array(b, score, wc, bc, ep, kp, nk, nek, bh, mh, fh)
    local self = setmetatable({}, Position)
    self._b = b
    self.score = score
    self.wc = wc
    self.bc = bc
    self.ep = ep
    self.kp = kp
    self._king = nk
    self._eking = nek
    self._bh = bh
    self._mh = mh
    self._fh = fh
    return self
end

-------------------------------------------------------------------------------
-- Pooled Position/board reuse for the search hot path
--
-- Every bound() node creates a child via move() (and a rotate() child for the
-- null move). In the immutable design those children allocate a fresh 120-slot
-- board + Position object each: measured ~1 board + ~1 object per node
-- (~23k tables + ~11.6k objects per 10k-node search), all short-lived garbage
-- on a GC'd interpreter.
--
-- The children's lifetimes are strictly nested with the recursion: each child
-- is passed to the next bound() frame, fully consumed there (only numbers --
-- scores and packed moves -- escape), and is dead before the next sibling is
-- created. So a simple free-list pool is safe: a pooled slot is only reused
-- after its position has fully returned. The pool is bounded (POOL_CAP slots);
-- beyond that, freed slots drop out and GC reclaims them.
--
-- move()/rotate() take a `pooled` flag: the search passes true, the public API
-- (sunfish.move, ai_move's returned position, tests calling rotate()) passes
-- nothing and keeps allocating fresh.
local POOL_CAP = 1024
local pool_free = {} -- free list of dead Position objects, each still holding its _b

local function pool_alloc()
    local self = pool_free[#pool_free]
    if self then
        pool_free[#pool_free] = nil
        return self, self._b
    end
    local b = {}
    local self = setmetatable({}, Position)
    self._b = b
    return self, b
end

local function pool_free_pos(self)
    -- Drop all fields so a stale reference can never alias a live board; the
    -- board (_b) stays on the object so it is reused with the slot.
    self.board = nil
    self._key = nil
    self._king = nil
    self._eking = nil
    self._bh = nil
    self._mh = nil
    self._fh = nil
    self.score = nil
    self.wc = nil
    self.bc = nil
    self.ep = nil
    self.kp = nil
    if #pool_free < POOL_CAP then
        pool_free[#pool_free + 1] = self
    end
end

-- Flag hash: the wc/bc/ep/kp contribution to the Zobrist key, computed from
-- explicit flag values (the child's flags differ from the parent's after
-- move()/rotate()). Used by the dual-hash incremental path.
local function flag_hash(wc, bc, ep, kp)
    local h = 0
    if wc[1] then h = h + zob_wc[1] end
    if wc[2] then h = h + zob_wc[2] end
    if bc[1] then h = h + zob_bc[1] end
    if bc[2] then h = h + zob_bc[2] end
    if ep ~= 0 then h = h + zob_ep[ep] end
    if kp ~= 0 then h = h + zob_kp[kp] end
    return h
end

-- Compute (and cache) the integer key for a position. The key is stored on the
-- position as `_key` so it is computed once per position, not per TT probe.
-- Search children carry `_bh`/`_mh`/`_fh` threaded through move()/rotate(), so
-- this is O(1) for them; public positions (built from strings) fall back to
-- the full 120-pass, which also populates the three hashes so their children
-- become incremental.
--
-- NOTE: this was previously reverted because a pre-existing `_b` corruption
-- (stale ep from the null-move rotate) made a cached board hash unsafe. That
-- bug is now fixed (rotate() clears ep, is_legal guards the ep capture), so the
-- cached hash is safe again — see the doc's "En-passant undo bug" note.
function Position:key()
    local k = self._key
    if not k then
        if self._bh then
            k = (self._bh + self._fh) % 4294967296
        else
            local b = self._b or self:ensure_arr()
            local zf = zflat
            local zm = zmirror
            local bh = 0
            local mh = 0
            for i = 1, 120 do
                local pc = b[i]
                -- Skip empty squares and the padding codes (98/99); only pieces hash.
                if pc ~= EMPTY and pc ~= SP and pc ~= NL then
                    bh = bh + zf[(pc + 6) * 120 + i]
                    mh = mh + zm[(pc + 6) * 120 + i]
                end
            end
            local fh = flag_hash(self.wc, self.bc, self.ep, self.kp)
            self._bh = bh
            self._mh = mh
            self._fh = fh
            k = (bh + fh) % 4294967296 -- keep it a 32-bit-range integer for cheap math
        end
        self._key = k
    end
    return k
end

-- Build the integer array from the string board (lazy, once).
function Position:ensure_arr()
    local b = self._b
    if not b then
        b = {}
        local board = self.board
        local b2c = byte_to_code
        for i = 1, 120 do
            b[i] = b2c[string.byte(board, i)]
        end
        self._b = b
    end
    return b
end

-- Materialize the string board from the integer array (lazy, once).
function Position:ensure_board()
    if not self.board then
        local b = self._b
        local c2c = code_to_char
        local parts = {}
        for i = 1, 120 do
            parts[i] = c2c[b[i]]
        end
        self.board = table.concat(parts)
    end
    return self.board
end

function Position:genMoves(out, start)
    local moves = out or {}
    local move_idx = start or 1
    local b = self:ensure_arr()
    local wc1, wc2 = self.wc[1], self.wc[2]
    local ep, kp = self.ep, self.kp

    for si = 1, 64 do
        local i = real_squares[si]
        local p = b[i]
        if p >= P and p <= K then
            if p == P then
                -- Pawn: single push, double push, captures, ep.
                -- Off-board squares are padding (SP=99/NL=98), never EMPTY, so
                -- b[j] == EMPTY inherently rejects them — no is_on_board_1
                -- guard needed. The double push is confined to rank-2 pawns
                -- (i >= A1+N) whose j2 is always on board.
                local j = i + N
                if b[j] == EMPTY then
                    move_idx = emit_pawn(moves, move_idx, i, j)
                    -- Double push: original allows it whenever i >= A1+N and
                    -- the intermediate square is empty (the single push above
                    -- already verified b[i+N]==EMPTY). The last-rank special
                    -- case (e.g. a pawn on e1) is preserved.
                    if i >= A1 + N then
                        local j2 = i + 2 * N
                        if b[j2] == EMPTY then
                            move_idx = emit_pawn(moves, move_idx, i, j2)
                        end
                    end
                end
                -- Captures (diagonals). En passant when the target is empty but is ep.
                -- pawn_caps is sentinel-padded to 2 (0 terminates).
                local pc = pawn_caps[i]
                for c = 1, 2 do
                    j = pc[c]
                    if j == 0 then break end
                    local q = b[j]
                    if q < 0 or (q == EMPTY and j == ep) then
                        move_idx = emit_pawn(moves, move_idx, i, j)
                    end
                end
            elseif p == KN then
                local kt = knight_targets[i]
                for c = 1, 8 do
                    local j = kt[c]
                    if j == 0 then break end
                    if b[j] <= EMPTY then
                        moves[move_idx] = i * 128 + j + PACKED_ZERO_VAL; move_idx = move_idx + 1
                    end
                end
            elseif p == K then
                local kg = king_targets[i]
                for c = 1, 8 do
                    local j = kg[c]
                    if j == 0 then break end
                    if b[j] <= EMPTY then
                        moves[move_idx] = i * 128 + j + PACKED_ZERO_VAL; move_idx = move_idx + 1
                    end
                end
            else
                -- Sliders: bishop (di 5..8), rook (di 1..4), queen (di 1..8).
                -- Castling (original semantics): when a ROOK at A1 slides E (or
                -- H1 slides W) and the square beyond the current j holds the
                -- king, emit the king's two-square move toward the rook:
                --   rook at A1: king at j+E -> j+W  (queenside)
                --   rook at H1: king at j+W -> j+E  (kingside)
                local sd = slider_dirs_by_piece[p]
                for s = 1, #sd do
                    local di = sd[s]
                    local ray = ray_squares[i][di]
                    local castling = p == R and (di == 2 or di == 4) -- E or W
                    local c = 1
                    while true do
                        local j = ray[c]
                        if j == 0 then break end
                        local q = b[j]
                        if q == EMPTY then
                            moves[move_idx] = i * 128 + j + PACKED_ZERO_VAL; move_idx = move_idx + 1
                        elseif q < 0 then
                            moves[move_idx] = i * 128 + j + PACKED_ZERO_VAL; move_idx = move_idx + 1
                            break
                        else
                            -- own piece blocks the ray, but if it's the king and
                            -- the rook has castling rights, emit the castling move
                            if castling then
                                if i == A1 and q == K and wc1 then
                                    moves[move_idx] = j * 128 + (j - 2) + PACKED_ZERO_VAL; move_idx = move_idx + 1
                                elseif i == H1 and q == K and wc2 then
                                    moves[move_idx] = j * 128 + (j + 2) + PACKED_ZERO_VAL; move_idx = move_idx + 1
                                end
                            end
                            break
                        end
                        c = c + 1
                    end
                end
            end
        end
    end
    return move_idx - 1 -- end index (count of moves written into `out`)
end

-------------------------------------------------------------------------------
-- Legality: the engine is a *chess* engine, so we enforce real chess rules.
-- A move is legal only if it does not leave the side-to-move's own king in
-- check, and captures of a protected enemy king are rejected (a king may
-- only be captured when the game has already been decided by checkmate).
-- genMoves() stays pseudo-legal for backward compatibility; legal_moves()
-- filters it. Search and public move validation use legal_moves().
-------------------------------------------------------------------------------

-- Is square `i` attacked by any opponent (lowercase) piece?
-- `i` is 0-indexed as in genMoves. Works on the integer array `_b`.
-- `b` is the board array; if nil it is fetched lazily (used by public paths).
-- `ptag` (E instrumentation) is the caller-class tag counted when
-- SUNFISH_PROFILE_ATTACKED=1: "probe"/"king"/"touch"/"ep"/"castle".
function Position:attacked(i, b, ptag)
    if PROF_ATTACKED then
        if ptag == "probe" then acnt_probe = acnt_probe + 1
        elseif ptag == "king" then acnt_king = acnt_king + 1
        elseif ptag == "touch" then acnt_touch = acnt_touch + 1
        elseif ptag == "ep" then acnt_ep = acnt_ep + 1
        elseif ptag == "castle" then acnt_castle = acnt_castle + 1 end
    end
    b = b or self:ensure_arr()

    -- King attacks (opponent kings). king_targets is sentinel-padded to 8.
    local kg = king_targets[i]
    for c = 1, 8 do
        local sq = kg[c]
        if sq == 0 then break end
        if b[sq] == -K then return true end
    end

    -- Knight attacks
    local kt = knight_targets[i]
    for c = 1, 8 do
        local sq = kt[c]
        if sq == 0 then break end
        if b[sq] == -KN then return true end
    end

    -- Pawn attacks: an enemy pawn attacks square i along one diagonal. The
    -- engine rotates the board after every move, so the enemy pawn's "forward"
    -- can point toward index 0 (white frame) or index 119 (black frame).
    -- Checking both diagonals is safe: in a legal position an enemy pawn can
    -- only be on one diagonal from i, and the other can't be occupied by a
    -- pawn (it would be behind the pawn).
    local pc = pawn_caps[i]
    for c = 1, 2 do
        local sq = pc[c]
        if sq == 0 then break end
        if b[sq] == -P then return true end
    end

    -- Sliding pieces (rook, bishop, queen): walk the 8 precomputed rays.
    -- Rook rays (di 1..4) attack with -R/-Q; bishop rays (di 5..8) with -B/-Q.
    for di = 1, 4 do
        local ray = ray_squares[i][di]
        local c = 1
        while true do
            local sq = ray[c]
            if sq == 0 then break end
            local q = b[sq]
            if q == -R or q == -Q then return true end
            if q ~= EMPTY then break end
            c = c + 1
        end
    end
    for di = 5, 8 do
        local ray = ray_squares[i][di]
        local c = 1
        while true do
            local sq = ray[c]
            if sq == 0 then break end
            local q = b[sq]
            if q == -B or q == -Q then return true end
            if q ~= EMPTY then break end
            c = c + 1
        end
    end

    return false
end

-- Is the side to move in check? (own king is code K)
function Position:in_check()
    local king = self:king_index()
    if king then
        return self:attacked(king, self._b)
    end
    return false
end

-- Find the index of the side-to-move's king, or nil. Cached on the position
-- (`_king`) because the king's square only changes on a king move and positions
-- are immutable (is_legal's in-place board mutations always undo before return).
function Position:king_index()
    local k = self._king
    if not k then
        local b = self:ensure_arr()
        for i = 1, 120 do
            if b[i] == K then
                k = i
                self._king = i
                return i
            end
        end
    end
    return k
end

-- Find the index of the enemy king (code -K), or nil. Cached on the position
-- (`_eking`) for the same reason as `_king`. Only used to thread the child's
-- king indices through move()/rotate(); public positions scan on first use.
function Position:eking_index()
    local k = self._eking
    if not k then
        local b = self:ensure_arr()
        for i = 1, 120 do
            if b[i] == -K then
                k = i
                self._eking = i
                return i
            end
        end
    end
    return k
end

-- Pin/check detection (replaces king_sensitive): computes, for the side to
-- move's king at `king`, the checks and pins in one 8-ray walk plus the
-- fixed-attacker probes (enemy king/knights/pawns). Returns:
--   nch            : number of checkers (0, 1, or 2+)
--   chk[1..2]      : the checker squares (only the first two are stored; a
--                    position with 2+ checkers only allows king moves anyway)
--   n_pinned       : number of pinned own pieces
--   pin[sq]        : truthy (== generation) when square `sq` is pinned
--   pin_dir[sq]    : the ray direction (di) along which `sq` is pinned
--   pg             : the generation tag for pin/pin_dir
-- The pinned direction is recovered from on_ray[king*121 + sq] when needed, so
-- pin_dir is only filled for the pinned pieces (cheap).
local chk_tmp, pin_tmp, pdir_tmp = {}, {}, {}
local pin_gen = 0
local function compute_check_pins(b, king)
    pin_gen = pin_gen + 1
    local g = pin_gen
    local nch, npin = 0, 0
    local c0, c1 = 0, 0

    -- Fixed attackers: enemy king, knights, pawns (mirror of attacked()).
    local kg = king_targets[king]
    for c = 1, #kg do
        if b[kg[c]] == -K then
            nch = nch + 1; if c0 == 0 then c0 = kg[c] elseif c1 == 0 then c1 = kg[c] end
        end
    end
    local kt = knight_targets[king]
    for c = 1, #kt do
        if b[kt[c]] == -KN then
            nch = nch + 1; if c0 == 0 then c0 = kt[c] elseif c1 == 0 then c1 = kt[c] end
        end
    end
    local pc = pawn_caps[king]
    for c = 1, #pc do
        if b[pc[c]] == -P then
            nch = nch + 1; if c0 == 0 then c0 = pc[c] elseif c1 == 0 then c1 = pc[c] end
        end
    end

    -- Slider checkers + pins: walk the 8 rays.
    for di = 1, 8 do
        local ray = ray_squares[king][di]
        local first, second = 0, 0
        local c = 1
        while true do
            local sq = ray[c]
            if sq == 0 then break end
            local v = b[sq]
            if v ~= EMPTY then
                if first == 0 then first = sq
                elseif second == 0 then second = sq
                else break end
            end
            c = c + 1
        end
        if first ~= 0 then
            local fv = b[first]
            if fv < 0 then
                -- Enemy slider attacking along this ray (rook on di 1..4,
                -- bishop on di 5..8, queen either) = checker.
                local atk = fv == -R and (di <= 4) or fv == -B and (di >= 5) or fv == -Q
                if atk then
                    nch = nch + 1
                    if c0 == 0 then c0 = first elseif c1 == 0 then c1 = first end
                end
            elseif fv >= P and fv <= K and second ~= 0 then
                -- Own piece first, enemy slider second = pinned.
                local sv = b[second]
                if sv < 0 then
                    local atk = sv == -R and (di <= 4) or sv == -B and (di >= 5) or sv == -Q
                    if atk then
                        npin = npin + 1
                        pin_tmp[first] = g
                        pdir_tmp[first] = di
                    end
                end
            end
        end
    end

    chk_tmp[1], chk_tmp[2] = c0, c1
    return nch, chk_tmp, npin, pin_tmp, pdir_tmp, g
end

-- Is the given pseudo-legal move legal? Uses the pin/check detection computed
-- once per position (see compute_check_pins) to decide most moves without any
-- board mutation:
--   - 2+ checkers -> only king moves are legal.
--   - 1 checker  -> a non-king move is legal iff it captures the checker or
--                   blocks the between-square (for a slider checker). A pinned
--                   piece may still capture the checker if the checker lies
--                   along the pin ray (capture along the pin line).
--   - 0 checkers -> every non-king move is legal unless the moving piece is
--                   pinned and the move leaves its pin ray.
-- King moves, en-passant, and castling keep the mutate/undo attacked() slow
-- path (they need the x-ray-correct attack test after a real board change).
-- `king` (optional) is the index of the own king; `nch`, `chk`, `pin`, `pdir`,
-- `pg` are the compute_check_pins result (see above). When they are nil the
-- full mutate/undo slow path is used (public API), which is always correct.
function Position:is_legal(move, king, nch, chk, pin, pdir, pg, b)
    local i, j = move_from(move), move_to(move)
    b = b or self:ensure_arr()
    local p = b[i]
    local q = b[j]

    -- Standard chess has no king captures, and no piece may land on the own
    -- king. A move onto either king is illegal. (The own-king case was missed:
    -- only q == -K was rejected, so a move like h1e1 with the king on e1
    -- overwrote the king and was wrongly accepted — caught by the selfplay
    -- correctness gate.)
    if q == K or q == -K then
        return false
    end

    if not king then
        king = self:king_index()
    end

    -- King move: destination (and castling intermediate square) must not be
    -- attacked. This is the x-ray-correct slow path — a king may not step into
    -- a square attacked by a slider even if a piece currently shields it.
    if p == K then
        if j - i == 2 or i - j == 2 then
            -- Castling. Replicate the original construction exactly: i emptied,
            -- between holds the KING, j holds the ROOK (the original put K at j
            -- then overwrote j with R), and the rook's origin square is left
            -- untouched. Attack-tests between and j.
            local between = j < i and i - 1 or i + 1
            b[i] = EMPTY
            b[between] = K
            b[j] = R
            local legal
            if self:attacked(between, b, "castle") then
                legal = false
            else
                legal = not self:attacked(j, b, "castle")
            end
            -- undo
            b[i] = K
            b[between] = EMPTY
            b[j] = q
            return legal
        end
        b[i] = EMPTY
        b[j] = K
        local legal = not self:attacked(j, b, "king")
        b[i] = K
        b[j] = q
        return legal
    end

    -- Non-king move. The fast path needs the precomputed checks/pins; without
    -- them, fall back to the mutate/undo attacked() test (always correct).
    if not nch then
        b[i] = EMPTY
        b[j] = p
        local legal = not self:attacked(king, b, "touch")
        b[i] = p
        b[j] = q
        return legal
    end

    if nch == 0 then
        -- No checkers: legal unless the piece is pinned and leaves its pin ray.
        if pin[i] == pg then
            -- Pinned: the destination must stay on the pin line (capture along
            -- the pin ray or a move along it). Moving along the pin direction
            -- toward/away from the king keeps the shield; off-line is illegal.
            local di = pdir[i]
            if on_ray[king * 121 + j] ~= di then
                return false
            end
        end
        -- En-passant: removing the captured pawn can expose a pin/check. The
        -- ep square j is on the pin line only for the rare horizontal case; the
        -- diagonal ep removes a pawn that could be the pinning slider. Run the
        -- mutate/undo slow path for correctness.
        local is_ep = p == P and ((j - i) == N + W or (j - i) == N + E) and q == EMPTY
        if is_ep then
            b[i] = EMPTY
            b[j] = p
            local ep_undo = false
            if b[j + S] == -P then
                b[j + S] = EMPTY
                ep_undo = true
            end
            local legal = not self:attacked(king, b, "ep")
            b[i] = p
            b[j] = q
            if ep_undo then b[j + S] = -P end
            return legal
        end
        return true
    end

    -- 1 checker: capture the checker or block the between-square.
    local chk1 = chk[1]
    local cv = b[chk1]
    local ok = false
    -- A pawn may capture the checker en passant: the destination j is not the
    -- checker square, but the captured pawn (at j+S) is. This is how a pawn
    -- resolves a check from a just-double-pushed pawn.
    local is_ep = p == P and ((j - i) == N + W or (j - i) == N + E) and q == EMPTY and (j + S) == chk1
    if j == chk1 or is_ep then
        -- Capturing the checker (directly, or via ep). If the moving piece is
        -- pinned, the capture is only legal when the checker lies on the pin
        -- ray (capture along the pin line keeps the shield). Otherwise any
        -- capture of the checker is legal.
        if pin[i] == pg then
            if on_ray[king * 121 + chk1] == pdir[i] then
                ok = true
            end
        else
            ok = true
        end
    elseif cv == -R or cv == -B or cv == -Q then
        -- Slider checker: block the between-square. j must lie STRICTLY between
        -- the king and the checker on the checker's ray: j on the king's ray in
        -- the checker's direction, and the checker continuing on the same ray
        -- from j (this excludes squares beyond the checker).
        local kc = on_ray[king * 121 + chk1]
        if on_ray[king * 121 + j] == kc and on_ray[j * 121 + chk1] == kc then
            ok = true
        end
    end
    return ok
end

-- All legal moves (filtered from pseudo-legal genMoves).
function Position:legal_moves()
    local pseudo = {}
    local pe = self:genMoves(pseudo, 1)
    local legal = {}
    local n = 0
    local king = self:king_index()
    local b = self._b
    local nch, chk, npin, pin, pdir, pg = compute_check_pins(b, king)
    for k = 1, pe do
        local m = pseudo[k]
        if self:is_legal(m, king, nch, chk, pin, pdir, pg, b) then
            n = n + 1
            legal[n] = m
        end
    end
    return legal
end

-- Checkmate: in check and no legal moves. Stalemate: not in check and no
-- legal moves.
function Position:is_checkmate()
    if not self:in_check() then return false end
    return #self:legal_moves() == 0
end

function Position:is_stalemate()
    if self:in_check() then return false end
    return #self:legal_moves() == 0
end

function Position:rotate(pooled)
    -- One pass over the integer array: reverse (k -> 119-k) and negate the
    -- piece codes (case swap). Padding codes are untouched.
    -- `pooled` (search null-move) reuses a pooled Position + board.
    local b = self:ensure_arr()
    local child, nb
    if pooled then
        child, nb = pool_alloc()
    else
        nb = {}
        child = nil
    end
    for k = 1, 120 do
        local v = b[121 - k]
        -- EMPTY (0) negates to 0, pieces negate (case swap), padding codes
        -- 98/99 pass through: a single ternary replaces the 3-way branch.
        nb[k] = (v == 98 or v == 99) and v or -v
    end
    -- Thread king indices: after rotation, own king = mirror of parent's enemy
    -- king; enemy king = mirror of parent's own king. If either king is absent
    -- (e.g. a test position with only one king), fall back to the lazy scan.
    local ek = self._eking or self:eking_index()
    local ok = self._king or self:king_index()
    local nk, nek = nil, nil
    if ok and ek then nk = 121 - ek end
    if ek and ok then nek = 121 - ok end
    -- rotate() is a NULL move (no pawn pushed): the en-passant target must be
    -- cleared, not mirrored from the parent. Mirroring a stale `ep` makes
    -- genMoves emit bogus ep captures and is_legal's undo corrupts the board
    -- (a phantom pawn leaks into the pooled child). kp mirrors correctly (it is
    -- a king-past-square, not an ep flag).
    local ep, kp = 0, 121 - self.kp
    -- Dual-hash threading (item 2): a pure rotation maps the board hash to the
    -- parent's mirror hash, and vice versa (rotate twice = identity on the
    -- mirror relation). Flags swap wc/bc and mirror kp (ep is cleared above).
    local fh = flag_hash(self.bc, self.wc, 0, kp)
    local bh, mh = self._mh, self._bh
    if pooled then
        child._b = nb
        child.score = -self.score
        child.wc = self.bc
        child.bc = self.wc
        child.ep = ep
        child.kp = kp
        child._king = nk
        child._eking = nek
        child._bh = bh
        child._mh = mh
        child._fh = fh
        return child
    end
    return Position.from_array(nb, -self.score, self.bc, self.wc, ep, kp, nk, nek, bh, mh, fh)
end

-- Zobrist contribution of a piece code at a square, for a hash table (zflat or
-- zmirror). Padding codes (98/99) and empty (0) contribute 0. Module-level so
-- move()'s dual-hash edit deltas don't allocate a closure per call.
local function zc(z, pc, sq)
    if pc ~= EMPTY and pc ~= SP and pc ~= NL then
        return z[(pc + 6) * 120 + sq]
    end
    return 0
end

function Position:move(move, val, pooled)
    local i, j, promo
    if type(move) == 'table' then
        i, j = move[1], move[2] -- public path: parsed UCI tuple
        promo = 0
        val = nil
    else
        i, j = move_from(move), move_to(move) -- internal: packed int
        promo = move_promo(move)
    end
    local b = self:ensure_arr()
    local p = b[i]
    local q = b[j]

    local score = self.score + (val or self:value(move, b))
    local wc, bc, ep, kp = self.wc, self.bc, 0, 0

    if i == A1 then wc = { false, wc[2] } end
    if i == H1 then wc = { wc[1], false } end
    if j == A8 then bc = { bc[1], false } end
    if j == H8 then bc = { false, bc[2] } end

    if p == K then
        wc = { false, false }
        if j - i == 2 or i - j == 2 then
            kp = math_floor((i + j) / 2)
        end
    end

    if p == P and j - i == 2 * N then
        ep = i + N
    end

    -- Build the rotated child board as a straight copy + sparse edits (the
    -- "make/unmake" experiment: measured ~5.5% faster than the single-pass
    -- rotate loop under LuaJ — the per-cell branch + interleaved edit/hash
    -- handling in the old loop costs more than a copy + a few overwrites).
    -- The moved-to square (j) becomes 121-j in the new frame (negated piece
    -- code = opposite color); the moved-from square (i) becomes 121-i and is
    -- emptied. Castling: the rook origin (A1/H1) is emptied and the rook lands
    -- on 121-kp. Promotion: any piece landing on rank 8 becomes a negated
    -- queen (the engine's promotion inference). En passant: the captured pawn
    -- at j+S is emptied.
    -- `pooled` (search path) reuses a pooled Position + board; otherwise a
    -- fresh object + table is allocated (public API / tests).
    local child, nb
    if pooled then
        child, nb = pool_alloc()
    else
        nb = {}
        child = nil
    end
    -- Copy the parent's board into the child (rotated frame) in one pass, then
    -- apply the sparse edits directly.
    for k = 1, 120 do
        local v = b[121 - k]
        nb[k] = (v == 98 or v == 99) and v or -v
    end
    local r = 121 - j
    local s = 121 - i
    -- Promotion piece: encoded promo (packed path), or queen for a table-path
    -- pawn reaching the last rank; otherwise the moving piece itself.
    local dest = promo ~= 0 and -promo
        or (p == P and A8 <= j and j <= H8 and -Q or -p)
    nb[r] = dest
    nb[s] = EMPTY
    if p == K and (j - i == 2 or i - j == 2) then
        -- Castling: rook origin (A1/H1 in the child frame) empties, rook
        -- lands on the king's between square (child frame 121-kp).
        local rook_from = j < i and A1 or H1
        nb[121 - rook_from] = EMPTY
        nb[121 - kp] = -R
    end
    if p == P and ((j - i) == N + W or (j - i) == N + E) and q == EMPTY then
        nb[121 - (j + S)] = EMPTY -- en passant
    end
    -- Dual-hash threading (item 2): the child is the rotated parent plus sparse
    -- edits, so child._bh = parent._mh + sum(edit deltas) and child._mh =
    -- parent._bh + sum(edit deltas). The child board already has the edits;
    -- here we only accumulate the hash deltas for the edited squares so key()
    -- stays O(1). Each edit at child square k replaces the pure-rotation value
    -- rot_pc = -parent[121-k] with the actual new_pc. Only meaningful when the
    -- parent already carries hashes (search path); public positions leave them
    -- nil and key() falls back to the 120-pass.
    local zf, zm = zflat, zmirror
    local dbh, dmh = 0, 0
    local has_hashes = self._bh ~= nil and self._mh ~= nil
    if has_hashes then
        -- Accumulate the Zobrist deltas for the edited squares inline. The
        -- old `add_edit` closure was a per-call LuaClosure allocation on every
        -- search child; the module-level `zc` helper (created once) + inline
        -- arithmetic is behavior-identical. Each edit at child square k
        -- replaces the pure-rotation value rot_pc = -parent[121-k] with the
        -- actual new_pc.
        local rot_r = -b[121 - r]
        dbh = dbh + zc(zf, dest, r) - zc(zf, rot_r, r)
        dmh = dmh + zc(zm, dest, r) - zc(zm, rot_r, r)
        local rot_s = -b[121 - s]
        dbh = dbh + zc(zf, EMPTY, s) - zc(zf, rot_s, s)
        dmh = dmh + zc(zm, EMPTY, s) - zc(zm, rot_s, s)
        if p == K and (j - i == 2 or i - j == 2) then
            local rook_from = j < i and A1 or H1
            local rf = 121 - rook_from
            local rot_rf = -b[121 - rf]
            dbh = dbh + zc(zf, EMPTY, rf) - zc(zf, rot_rf, rf)
            dmh = dmh + zc(zm, EMPTY, rf) - zc(zm, rot_rf, rf)
            local kpf = 121 - kp
            local rot_kpf = -b[121 - kpf]
            dbh = dbh + zc(zf, -R, kpf) - zc(zf, rot_kpf, kpf)
            dmh = dmh + zc(zm, -R, kpf) - zc(zm, rot_kpf, kpf)
        end
        if p == P and ((j - i) == N + W or (j - i) == N + E) and q == EMPTY then
            local epf = 121 - (j + S)
            local rot_epf = -b[121 - epf]
            dbh = dbh + zc(zf, EMPTY, epf) - zc(zf, rot_epf, epf)
            dmh = dmh + zc(zm, EMPTY, epf) - zc(zm, rot_epf, epf)
        end
    end
    -- Thread king indices: child own king = mirror of parent's enemy king;
    -- child enemy king = mirror of parent's own king, or of the king's new
    -- square when the king itself moved. If either king is absent, fall back
    -- to the lazy scan.
    local ek = self._eking or self:eking_index()
    local ok = self._king or self:king_index()
    local nk, nek = nil, nil
    if ek then nk = 121 - ek end
    if ok then nek = 121 - (p == K and j or ok) end
    local fh = flag_hash(bc, wc, 121 - ep, 121 - kp)
    local bh, mh = nil, nil
    if has_hashes then
        bh = (self._mh + dbh) % 4294967296
        mh = (self._bh + dmh) % 4294967296
    end
    if pooled then
        -- Reuse the pooled object: set the new frame fields on it.
        child._b = nb
        child.score = -score
        child.wc = bc
        child.bc = wc
        child.ep = 121 - ep
        child.kp = 121 - kp
        child._king = nk
        child._eking = nek
        child._bh = bh
        child._mh = mh
        child._fh = fh
        return child
    end
    return Position.from_array(nb, -score, bc, wc, 121 - ep, 121 - kp, nk, nek, bh, mh, fh)
end

function Position:value(move, b)
    local i, j, promo
    if type(move) == 'table' then
        i, j = move[1], move[2] -- public path: parsed UCI tuple
        promo = 0
    else
        i, j = move_from(move), move_to(move) -- internal: packed int
        promo = move_promo(move)
    end
    b = b or self:ensure_arr()
    local p = b[i]
    local q = b[j]

    -- Squares i/j are 1-based; pst tables are keyed 1-based (pst[piece][sq]).
    local pp = pst[p]
    local score = pp[j] - pp[i]
    if q < 0 then
        score = score + pst[-q][j] -- captured piece's PST value
    end

    local kp = self.kp
    if j - kp < 2 and kp - j < 2 then
        score = score + pst[K][j]
    end

    if p == K and (j - i == 2 or i - j == 2) then
        score = score + pst[R][math_floor((i + j) / 2)]
        score = score - pst[R][j < i and A1 or H1]
    end

    if p == P then
        -- Promotion: value the encoded promotion piece (packed path), else queen
        -- for a table-path pawn reaching the last rank.
        if promo ~= 0 then
            score = score + pst[promo][j] - pst[P][j]
        elseif A8 <= j and j <= H8 then
            score = score + pst[Q][j] - pst[P][j]
        end
        if j == self.ep then
            score = score + pst[P][j + S]
        end
    end
    return score
end

-- Fixed-size transposition table as five parallel arrays (no per-slot table
-- chase, no temp table at the store site). Probing uses key % TT_SIZE and
-- verifies ttK[s] == key (full 32-bit key), so hash collisions only cause a
-- missed entry, never a wrong result.
local ttK = {}
local ttD = {}
local ttS = {}
local ttG = {}
local ttM = {}

-- Pre-size the five parallel arrays at module load. Lua tables grow
-- incrementally; without this the first search pays several rehashes while
-- filling up to TT_SIZE integer keys *during the timed region*. Filling with
-- sentinels (ttK = -1) allocates the array part once; a probe verifies
-- ttK[s] == key, so the sentinel can never be a false match (keys are >= 0).
for s = 1, TT_SIZE do
    ttK[s] = -1
    ttD[s] = 0
    ttS[s] = 0
    ttG[s] = 0
    ttM[s] = 0
end

local function tp_set(key, depth, score, gamma, move)
    local s = key % TT_SIZE + 1
    ttK[s] = key
    ttD[s] = depth
    ttS[s] = score
    ttG[s] = gamma
    ttM[s] = move
end

-- F2 measurement counters. The probe itself is inlined in bound()/search()
-- (item 5); these keep sunfish.tt_stats accurate.
local tt_probe = 0 -- F2 measurement: total probes
local tt_hit = 0   -- F2 measurement: probes that found a matching full key
local tt_slot_hit = 0 -- F2 measurement: probes whose slot was occupied (any key)

-------------------------------------------------------------------------------
-- Search logic
-------------------------------------------------------------------------------

local nodes = 0
local yield_left = YIELD_QUANTUM -- countdown for the periodic coroutine yield

-- Budget-aware stop (F1): `maxn` is threaded through bound() so it can abort
-- mid-depth when the node budget is exhausted; TIME_BUDGET (seconds, 0=off)
-- adds a wall-clock deadline at the same per-node check point. budget_exhausted
-- is set by bound() and read by search() to break out of the depth loop.
local budget_exhausted = false
local TIME_BUDGET = 0
local time_deadline = 0

-- Hoist hot Position methods to upvalues: each `pos:method()` is a table read
-- that misses into __index (a metamethod event + function lookup). Binding
-- once and calling as plain functions removes that indirection from the
-- per-node hot path (~10 dispatches per node).
local m_genMoves = Position.genMoves
local m_king_index = Position.king_index
local m_is_legal = Position.is_legal
local m_in_check = Position.in_check
local m_key = Position.key
local m_rotate = Position.rotate
local m_value = Position.value
local m_move = Position.move

-------------------------------------------------------------------------------
-- Pooled move buffer + count-driven sort (search hot path)
--
-- genMoves writes packed moves into a caller-provided array and returns the
-- end index; bound() filters/sorts in place on a module-level reusable buffer.
-- No `#` (LuaJ's rawlen is a binary search), no table.sort null-scan, no tail
-- clear -- the explicit count says exactly how many entries are live. Entries
-- beyond the count are stale but never read. This removes the per-node list
-- table allocation that the earlier scratch-pooling attempt could not (it
-- relied on `#`, whose bookkeeping overhead made it slower).
--
-- The comparator is `move_greater` below (value desc via packed integer,
-- tie-break i desc / j asc). Written as a plain heap sort with an explicit
-- count so we never need `#` or a nil boundary.

-- Per-depth move buffers: each bound() frame needs its own buffer because the
-- recursion overwrites shared storage while the outer frame still iterates its
-- sorted moves. Buffers are indexed by search depth (bounded ~10-20 plies),
-- so each frame reads/writes its own region; no clear needed (explicit count).
local move_stack = {}
local ply = 0 -- current recursion depth (incremented per bound() entry)

-- The packed-move layout (value in the high bits via VAL_SCALE = 2^14, coords
-- in the low 14 bits with max 119*128+119 = 15351 < 16384) makes a plain
-- integer comparison sort by value first, then by coordinates — exactly the
-- old `move_greater` ordering. move_set_val stores a full (val+VAL_BIAS)*VAL_SCALE
-- delta per entry, so same-node moves never differ by less than VAL_SCALE and
-- the i/j tie-break is unreachable on the search path. The comparator is
-- therefore dead code; the heap sort below compares packed ints directly.
-- NOTE: raw `a > b` on the whole integer reverses the tie-break order for
-- entries that differ only in the coordinate bits (< VAL_SCALE apart), but no
-- two moves of one node do, so the searched order is byte-identical.

-- In-place heap sort of buf[1..n] descending by packed-integer value.
-- Classic max-heap + extract-to-end produces ASCENDING; for descending we build
-- a MIN-heap (smallest at root) and extract to the end, so the largest lands
-- first. The comparator is inverted for the heap property. Comparisons are
-- inline raw `>` on the packed ints (no function calls; see the note above on
-- why the integer order matches the removed move_greater).
local function move_sort(buf, n)
    -- build min-heap (root is the smallest)
    for start = math_floor(n / 2), 1, -1 do
        local root = start
        while root * 2 <= n do
            local child = root * 2
            if child < n and buf[child] > buf[child + 1] then
                child = child + 1
            end
            if buf[root] > buf[child] then
                buf[root], buf[child] = buf[child], buf[root]
                root = child
            else
                break
            end
        end
    end
    -- extract min to the end -> descending order
    for endpos = n, 2, -1 do
        buf[1], buf[endpos] = buf[endpos], buf[1]
        local root = 1
        local m = endpos - 1
        while root * 2 <= m do
            local child = root * 2
            if child < m and buf[child] > buf[child + 1] then
                child = child + 1
            end
            if buf[root] > buf[child] then
                buf[root], buf[child] = buf[child], buf[root]
                root = child
            else
                break
            end
        end
    end
end

local function bound(pos, gamma, depth, maxn)
    nodes = nodes + 1
    -- Countdown-based yield: one decrement + compare per node (vs a modulo),
    -- and a coroutine switch only every YIELD_QUANTUM nodes.
    if YIELD_ENABLED then
        yield_left = yield_left - 1
        if yield_left == 0 then
            yield_left = YIELD_QUANTUM
            coroutine.yield()
        end
    end

    -- Budget-aware stop (F1): the depth loop only checks nodes >= maxn between
    -- depths, so a depth can overshoot the budget (depth 6 runs 15.8k vs
    -- NODES_SEARCHED=10k). Abort here at the per-node check point instead: when
    -- the budget is exhausted, set the flag and unwind to search(), which breaks
    -- out of the depth loop. The position's score is returned (the caller never
    -- uses the depth-6 result when the budget aborted — search() keeps the last
    -- completed depth's fail-high move).
    if nodes >= maxn and maxn > 0 then
        budget_exhausted = true
        return pos.score
    end
    if TIME_BUDGET > 0 and os.clock() >= time_deadline then
        budget_exhausted = true
        return pos.score
    end

    if pos.score >= MATE_VALUE or pos.score <= -MATE_VALUE then
        return pos.score
    end

    -- Look up the transposition table BEFORE move generation. A usable entry
    -- (same depth, bound satisfied) lets us return immediately without paying
    -- for genMoves + the legality filter. Mate/stalemate is safe: a position
    -- with no legal moves never stores a TT entry (we only store after a legal
    -- move is found), and the terminal-score check above catches
    -- already-decided positions.
    local key = m_key(pos)
    -- Inlined TT probe (item 5): the flat arrays are read directly with the
    -- slot computed once, and only after the full-key verify — no 4-value
    -- tp_get return, no ttD/ttS/ttG reads on a miss. The F2 measurement
    -- counters (tt_probe/tt_slot_hit/tt_hit) are kept so sunfish.tt_stats
    -- stays accurate. `ed` is also read by the store-site guard below, so it
    -- is declared here.
    local s = key % TT_SIZE + 1
    tt_probe = tt_probe + 1
    local had_entry = false
    local ed = -1
    if ttK[s] ~= -1 then
        tt_slot_hit = tt_slot_hit + 1
        if ttK[s] == key then
            tt_hit = tt_hit + 1
            had_entry = true
            ed = ttD[s]
            if ed >= depth then
                local es = ttS[s]
                local eg = ttG[s]
                if es < eg and es < gamma or es >= eg and es >= gamma then
                    return es, ttM[s]
                end
            end
        end
    end

    -- Generate pseudo-legal moves and filter out those that leave our own king
    -- in check. If no legal move exists the position is checkmate or stalemate.
    -- genMoves writes packed moves into this frame's per-ply buffer; we filter
    -- in place (nlegal <= k, so compaction never overwrites an unread entry).
    ply = ply + 1
    local buf = move_stack[ply]
    if not buf then
        buf = {}
        move_stack[ply] = buf
    end
    local pe = m_genMoves(pos, buf, 1)
    local nlegal = 0
    local king = m_king_index(pos)
    local b = pos._b
    local nch, chk, npin, pin, pdir, pg = compute_check_pins(b, king)
    for k = 1, pe do
        local move = buf[k]
        if m_is_legal(pos, move, king, nch, chk, pin, pdir, pg, b) then
            nlegal = nlegal + 1
            buf[nlegal] = move
        end
    end
    if nlegal == 0 then
        ply = ply - 1
        if m_in_check(pos) then
            return -MATE_VALUE -- checkmate: side to move loses
        else
            return 0 -- stalemate
        end
    end

    -- Null-move search. At depth <= 0 the recursion is skipped (the `or
    -- pos.score` short-circuit), so the rotated child's board is never read --
    -- creating it is pure waste on every leaf (the largest node class). Only
    -- build + free the child when depth > 0.
    local null_child
    local nullscore = pos.score
    if depth > 0 then
        null_child = m_rotate(pos, true) -- pooled: no alloc in the hot path
        nullscore = -bound(null_child, 1 - gamma, depth - 3, maxn)
        pool_free_pos(null_child) -- the null-move child is dead after this node
    end
    if nullscore >= gamma then
        ply = ply - 1
        return nullscore
    end

    local best, bmove = -3 * MATE_VALUE, nil

    -- Cache calculated move values so the sort doesn't repeatedly call `pos:value()` $O(N \log N)$ times
    for k = 1, nlegal do
        buf[k] = move_set_val(buf[k], m_value(pos, buf[k], b))
    end

    -- At depth <= 0 the loop below breaks at the first move_val < 150 (the
    -- tail is never searched), so filter to the >= 150 subset BEFORE sorting:
    -- the searched set and order are unchanged, but the heap sort only sees the
    -- kept subset (leaves are a large fraction of nodes). Compaction is in
    -- place (nlegal <= k, never overwrites an unread entry); the kept count is
    -- exact so no `#` or tail-clear is needed.
    local sort_n = nlegal
    if depth <= 0 then
        local keep = 0
        for k = 1, nlegal do
            if move_val(buf[k]) >= 150 then
                keep = keep + 1
                buf[keep] = buf[k]
            end
        end
        sort_n = keep
    end

    -- A 0-1 element sort is a no-op; skip the call at the many leaves with
    -- 0-1 kept moves.
    if sort_n > 1 then
        move_sort(buf, sort_n)
    end

    for k = 1, sort_n do
        local move = buf[k]
        local mv = move_val(move)
        local child = m_move(pos, move, mv, true) -- pooled
        local score = -bound(child, 1 - gamma, depth - 1, maxn)
        pool_free_pos(child) -- the child is dead after its subtree returns
        if score > best then
            best = score
            bmove = move
        end
        if score >= gamma then
            break
        end
    end

    if depth <= 0 and best < nullscore then
        ply = ply - 1
        return nullscore
    end

    if not had_entry or depth >= ed and best >= gamma then
        tp_set(key, depth, best, gamma, bmove)
    end
    ply = ply - 1
    return best, bmove
end

local function search(pos, maxn)
    maxn = maxn or NODES_SEARCHED
    nodes = 0
    budget_exhausted = false
    tt_probe, tt_hit, tt_slot_hit = 0, 0, 0
    acnt_probe, acnt_king, acnt_touch, acnt_ep, acnt_castle = 0, 0, 0, 0, 0
    if TIME_BUDGET > 0 then
        time_deadline = os.clock() + TIME_BUDGET
    end
    local score
    -- The move to return: the last fail-high (score >= gamma) bound call at the
    -- deepest completed depth. Capturing it directly avoids the post-loop TT
    -- re-probe, whose root slot can be overwritten by a deeper transposition
    -- (the `(pass)` UX artifact). Falls back to the re-probe when no bound call
    -- failed high (e.g. the very first call at depth 1 returns a bound score
    -- below gamma with no move yet).
    local rootmove

    -- F3 (aspiration): after depth 1, start the root window at the previous
    -- depth's score +/- ASPIRATION instead of the full [-3M, 3M] range, and
    -- widen (double) on a fail. Fewer root probes per depth when the score is
    -- stable. Node-count-changing (gate: oracle/perft/endgames, not invariant).
    local ASPIRATION = 100
    local prev_score = nil

    for depth = 1, 98 do
        local lower, upper
        if prev_score ~= nil then
            lower = prev_score - ASPIRATION
            upper = prev_score + ASPIRATION
        else
            lower, upper = -3 * MATE_VALUE, 3 * MATE_VALUE
        end
        while lower < upper - 3 do
            local gamma = math_floor((lower + upper + 1) / 2)
            local mv
            score, mv = bound(pos, gamma, depth, maxn)
            assert(score)
            if score >= gamma then
                lower = score
                rootmove = mv
            end
            if score < gamma then
                upper = score
            end
        end
        assert(score)
        -- F3 fail-widen: if the converged score is on/outside the aspiration
        -- window boundary, the true score lies outside it — re-search this depth
        -- with the full window so the result isn't clipped to the window edge.
        if prev_score ~= nil and (score <= prev_score - ASPIRATION or score >= prev_score + ASPIRATION) then
            local nlower, nupper = -3 * MATE_VALUE, 3 * MATE_VALUE
            while nlower < nupper - 3 do
                local ngamma = math_floor((nlower + nupper + 1) / 2)
                local nmv
                score, nmv = bound(pos, ngamma, depth, maxn)
                assert(score)
                if score >= ngamma then
                    nlower = score
                    rootmove = nmv
                end
                if score < ngamma then
                    nupper = score
                end
            end
        end
        prev_score = score

        if VERBOSE then
            print(string_format("Searched %d nodes. Depth %d. Score %d(%d/%d)", nodes, depth, score, lower, upper))
        end

        -- Budget-aware stop: break early when bound() aborted mid-depth (the
        -- flag is set at the per-node check point) or the depth completed the
        -- budget. The last completed depth's fail-high move is already captured.
        if budget_exhausted or nodes >= maxn or score >= MATE_VALUE or score <= -MATE_VALUE then
            break
        end
    end

    -- Validate the root move before returning it. The TT can return a move
    -- stored for a different transposition (the root slot can be overwritten
    -- by a deeper search), and a full-key collision would make it illegal here.
    -- The correctness gate (selfplay, stockfish-validated) caught exactly this:
    -- an illegal h1e1 was returned. Fall back to nil (engine passes) if the
    -- move isn't legal. ensure_arr() materializes _b for public positions.
    local root_b = pos:ensure_arr()
    local rk = m_king_index(pos)
    local rnch, rchk, rnpin, rpin, rpdir, rpg = compute_check_pins(root_b, rk)
    if rootmove ~= nil then
        if m_is_legal(pos, rootmove, rk, rnch, rchk, rpin, rpdir, rpg, root_b) then
            return rootmove, score
        end
        rootmove = nil
    end
    local ttmove
    local tk = m_key(pos)
    local tslot = tk % TT_SIZE + 1
    if ttK[tslot] == tk then
        ttmove = ttM[tslot]
    end
    if ttmove ~= nil then
        if m_is_legal(pos, ttmove, rk, rnch, rchk, rpin, rpdir, rpg, root_b) then
            return ttmove, score
        end
    end
    return nil, score
end

-------------------------------------------------------------------------------
-- User interface
-------------------------------------------------------------------------------

local function parse(c)
    if not c then return nil end
    local p, v = string_sub(c, 1, 1), string_sub(c, 2, 2)
    if not (p and v and tonumber(v)) then return nil end

    local fil, rank = string_byte(p) - string_byte('a'), tonumber(v) - 1
    return A1 + fil - 10 * rank
end

-- Internal 1-based square -> coordinate name. Takes a 1-based internal square
-- (A1=92, Phase 9 convention); the pre-Phase-9 0-based A1=91 made the engine's
-- own move rendering off by one (ai_move returned a8b6 for the real g1f3).
local function render(i)
    local rank, fil = math_floor((i - 92) / 10), (i - 92) % 10
    return string.char(fil + string_byte('a')) .. tostring(-rank + 1)
end

-- Public 0-based coordinate helpers (backward-compatible with the original
-- sunfish API: A1=91). They convert between the 0-based public square index
-- and the coordinate name; internal code uses the 1-based render() above.
local function render_public(i) -- 0-based public square (A1=91)
    local rank, fil = math_floor((i - 91) / 10), (i - 91) % 10
    return string.char(fil + string_byte('a')) .. tostring(-rank + 1)
end

local function parse_public(name) -- coordinate name -> 0-based public square
    local p, v = string_sub(name, 1, 1), string_sub(name, 2, 2)
    if not (p and v and tonumber(v)) then return nil end
    local fil, rank = string_byte(p) - string_byte('a'), tonumber(v) - 1
    return 91 + fil - 10 * rank
end

--//RPD interface:

local sunfish = {}

sunfish.MATE_VALUE = MATE_VALUE

-- Tunable yield behavior for the search coroutine (see the YIELD_QUANTUM
-- comment near the top of the file). Pass enable=false to disable yields
-- entirely (throughput ceiling; only safe where the caller never polls).
function sunfish.set_yield(quantum, enable)
    if quantum then YIELD_QUANTUM = quantum end
    if enable ~= nil then YIELD_ENABLED = enable end
    yield_left = YIELD_QUANTUM -- re-arm so the next search uses the new quantum
end

-- Wall-clock search budget (F1): limit one ai_move search to `seconds`
-- (0 disables; the default is unlimited). The deadline is checked at the same
-- per-node point as the node budget, so a search stops mid-depth instead of
-- overshooting. Note os.clock() is wall-clock under LuaJ, which is exactly
-- what a responsiveness deadline wants on the Android RPD layer.
function sunfish.set_time_budget(seconds)
    TIME_BUDGET = seconds or 0
end

-- Node budget per ai_move search (default NODES_SEARCHED). Raise for stronger
-- play, lower for faster response; exposed so the host can tune at runtime.
function sunfish.set_nodes(n)
    NODES_SEARCHED = n or NODES_SEARCHED
end

-- F2 measurement: TT probe/hit/occupancy stats from the last search.
function sunfish.tt_stats()
    return { probe = tt_probe, hit = tt_hit, slot_hit = tt_slot_hit }
end

-- E measurement: attacked() caller-class counts from the last search
-- (only populated when SUNFISH_PROFILE_ATTACKED=1).
function sunfish.attacked_stats()
    return { probe = acnt_probe, king = acnt_king, touch = acnt_touch,
             ep = acnt_ep, castle = acnt_castle }
end

local game = Position.new(initial, 0, { true, true }, { true, true }, 0, 0)

function sunfish.new()
    game = Position.new(initial, 0, { true, true }, { true, true }, 0, 0)
    return game
end

function sunfish.store_data(game)
    -- Materialize the string board and skip internal `_`-prefixed fields so
    -- the serialized shape stays board/score/wc/bc/ep/kp.
    game:ensure_board()
    local dta = {}
    for k, v in pairs(game) do
        if type(v) ~= 'function' and not (type(k) == 'string' and k:sub(1, 1) == '_') then
            dta[k] = v
        end
    end
    return dta
end

function sunfish.restore_data(dta)
    game = setmetatable({}, Position)
    for k,v in pairs(dta) do
        game[k] = v
    end
    return game
end

function sunfish.move(game, mv)
    local fromSq = parse(string_sub(mv, 1, 2))
    local toSq = parse(string_sub(mv, 3, 4))
    if not (fromSq and toSq) then
        return false
    end
    -- Promotion piece from the optional 5th UCI char. nil = unnamed promotion
    -- (defaults to queen) or a plain non-promotion move.
    local promoChar = #mv >= 5 and string_sub(mv, 5, 5) or nil
    local promoCode = promoChar and ({ q = Q, r = R, b = B, n = KN })[promoChar] or nil
    -- Validate ONE user move instead of building the whole legal_moves() list:
    -- generate pseudo-legal moves, find the matching packed move (by from/to and,
    -- for promotions, the piece), and run is_legal on only that move. This keeps
    -- sunfish.move snappy under LuaJ (legal_moves filters every pseudo-move
    -- through is_legal + attacked()).
    local pseudo = {}
    local pe = game:genMoves(pseudo, 1)
    local b = game:ensure_arr()
    local king = game:king_index()
    local nch, chk, npin, pin, pdir, pg = compute_check_pins(b, king)
    local chosen
    for k = 1, pe do
        local m = pseudo[k]
        if move_from(m) == fromSq and move_to(m) == toSq then
            local mp = move_promo(m)
            if promoCode then
                if mp == promoCode then chosen = m; break end        -- exact piece requested
            else
                if mp == 0 then chosen = m; break end                -- plain move
                if mp == Q then chosen = m end                       -- unnamed promotion -> queen
            end
        end
    end
    if chosen and game:is_legal(chosen, king, nch, chk, pin, pdir, pg, b) then
        local ng = game:move(chosen) -- packed: carries the promotion piece
        ng:ensure_board()
        return ng
    end
    return false
end

function sunfish.ai_move(game)
    local move, score = search(game)

    assert(score)
    if not move then
        -- Search converged to a mate/stalemate without a move (the root is
        -- already decided). Return the position unchanged and the score.
        game:ensure_board()
        return game, nil, score
    end

    game = game:move(move)
    game:ensure_board()

    local mv = render(121 - move_from(move)) .. render(121 - move_to(move))
    local promo = move_promo(move)
    if promo ~= 0 then
        mv = mv .. code_to_char[-promo] -- UCI promotion suffix (lowercase)
    end
    return game, mv, score
end

-- Position query helpers (backward-compatible additions).
function sunfish.in_check(game)
    return game:in_check()
end

function sunfish.is_checkmate(game)
    return game:is_checkmate()
end

function sunfish.is_stalemate(game)
    return game:is_stalemate()
end

function sunfish.legal_moves(game)
    return game:legal_moves()
end

-- Public: every legal move of the side to move, as UCI-style coordinate
-- strings ("e2e4"), rendered in the current position's own frame. Lets
-- integrations show/parse moves without touching the internal packed-move
-- encoding (move_from/move_to + 1-based render are module-local on purpose).
function sunfish.legal_moves_uci(game)
    local out = {}
    for _, m in ipairs(game:legal_moves()) do
        local s = render(move_from(m)) .. render(move_to(m))
        local promo = move_promo(m)
        if promo ~= 0 then s = s .. code_to_char[-promo] end
        out[#out + 1] = s
    end
    return out
end

function sunfish.move_2_cell(cell)
    -- Public API: 0-based square index (A1=91), matching the original sunfish.
    return render_public(cell)
end

function sunfish.cell_2_move(move)
    -- Public API: coordinate name -> 0-based square index (A1=91).
    return parse_public(string_sub(move, 1, 2))
end

return sunfish
