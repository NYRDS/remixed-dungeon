-- bkm_light.lua
--
-- Lightweight Bratko-Kopec-Michie-style KRK/KQK mover.
-- No large precomputed solver. Suitable for constrained environments.
--
-- Square encoding:
--   0..63, a1 = 0, h8 = 63
--
-- piece:
--   "R" for KRK
--   "Q" for KQK
--
-- pos:
--   {
--     wk  = strong king square,
--     pc  = rook/queen square,
--     bk  = weak king square,
--     stm = 0, -- 0 = strong side to move, 1 = weak side to move
--   }
--
-- Example:
--   local bkm = require("bkm_light")
--   local mv = bkm.best_move({
--     wk  = bkm.square("b6"),
--     pc  = bkm.square("h1"),
--     bk  = bkm.square("a8"),
--     stm = 0,
--   }, "R")
--   print(bkm.alg(mv.from), bkm.alg(mv.to)) --> h1 h8

local bkm = {}

---------------------------------------------------------------
-- Coordinates
---------------------------------------------------------------

function bkm.square(s)
  if type(s) == "number" then return s end
  local f = s:sub(1, 1):lower():byte() - ("a"):byte()
  local r = tonumber(s:sub(2, 2)) - 1
  if f < 0 or f > 7 or r < 0 or r > 7 then
    error("invalid square: " .. tostring(s))
  end
  return r * 8 + f
end

function bkm.alg(sq)
  if not sq then return "?" end
  return string.char(("a"):byte() + (sq % 8)) .. tostring(math.floor(sq / 8) + 1)
end

local function file(sq)
  return sq % 8
end

local function rank(sq)
  return math.floor(sq / 8)
end

---------------------------------------------------------------
-- Tiny precomputed tables
---------------------------------------------------------------

local king_attacks = {}
local edge_dist = {}
local cheb_dist = {}

for a = 0, 63 do
  local af, ar = file(a), rank(a)
  edge_dist[a + 1] = math.min(af, ar, 7 - af, 7 - ar)

  for b = 0, 63 do
    local bf, br = file(b), rank(b)
    local d = math.max(math.abs(af - bf), math.abs(ar - br))
    cheb_dist[a * 64 + b + 1] = d
  end
end

local function cheb(a, b)
  return cheb_dist[a * 64 + b + 1]
end

for s = 0, 63 do
  local f, r = file(s), rank(s)
  local t = {}
  for df = -1, 1 do
    for dr = -1, 1 do
      if df ~= 0 or dr ~= 0 then
        local nf, nr = f + df, r + dr
        if nf >= 0 and nf < 8 and nr >= 0 and nr < 8 then
          table.insert(t, nr * 8 + nf)
        end
      end
    end
  end
  king_attacks[s] = t
end

---------------------------------------------------------------
-- Directions
---------------------------------------------------------------

local DIRS = {
  R = {
    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
  },
  Q = {
    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
  },
}

---------------------------------------------------------------
-- Attacks
---------------------------------------------------------------

local function attacked(piece, pc, target, blocker)
  if pc == target then return false end

  local pf, pr = file(pc), rank(pc)
  local tf, tr = file(target), rank(target)
  local df, dr = tf - pf, tr - pr
  local adf, adr = math.abs(df), math.abs(dr)

  local ok
  if piece == "R" then
    ok = (df == 0 or dr == 0)
  else
    ok = (df == 0 or dr == 0 or adf == adr)
  end

  if not ok then return false end

  local sf = (df > 0 and 1) or (df < 0 and -1) or 0
  local sr = (dr > 0 and 1) or (dr < 0 and -1) or 0

  local f, r = pf + sf, pr + sr
  while f ~= tf or r ~= tr do
    local s = r * 8 + f
    if s == blocker then
      return false
    end
    f = f + sf
    r = r + sr
  end

  return true
end

local function is_check(wk, pc, bk, piece)
  return attacked(piece, pc, bk, wk)
end

---------------------------------------------------------------
-- Weak king moves
---------------------------------------------------------------

local function weak_moves(wk, pc, bk, piece)
  local moves = {}

  for _, to in ipairs(king_attacks[bk]) do
    if to == pc then
      -- Capture is legal only if the strong piece is not protected
      -- by the strong king.
      if cheb(to, wk) > 1 then
        table.insert(moves, { to = to, capture = true })
      end
    elseif to ~= wk then
      if cheb(to, wk) > 1 and not attacked(piece, pc, to, wk) then
        table.insert(moves, { to = to })
      end
    end
  end

  return moves
end

local function weak_mobility(wk, pc, bk, piece)
  local n = 0

  for _, to in ipairs(king_attacks[bk]) do
    if to == pc then
      if cheb(to, wk) > 1 then
        n = n + 1
      end
    elseif to ~= wk then
      if cheb(to, wk) > 1 and not attacked(piece, pc, to, wk) then
        n = n + 1
      end
    end
  end

  return n
end

---------------------------------------------------------------
-- Strong side moves
---------------------------------------------------------------

local function strong_moves(wk, pc, bk, piece)
  local moves = {}

  -- Strong king moves.
  for _, to in ipairs(king_attacks[wk]) do
    if to ~= pc and to ~= bk and cheb(to, bk) > 1 then
      table.insert(moves, {
        from = wk,
        to = to,
        piece = "K",
        wk = to,
        pc = pc,
        bk = bk,
      })
    end
  end

  -- Strong piece moves.
  for _, dir in ipairs(DIRS[piece]) do
    local df, dr = dir[1], dir[2]
    local f, r = file(pc) + df, rank(pc) + dr

    while f >= 0 and f < 8 and r >= 0 and r < 8 do
      local sq = r * 8 + f

      if sq == wk or sq == bk then
        break
      end

      table.insert(moves, {
        from = pc,
        to = sq,
        piece = piece,
        wk = wk,
        pc = sq,
        bk = bk,
      })

      f = f + df
      r = r + dr
    end
  end

  return moves
end

---------------------------------------------------------------
-- BKM-style static score.
-- Lower is better for the strong side.
---------------------------------------------------------------

-- Cycle-breaker: remember the recent states (wk,pc,bk,stm) the strong side
-- moved to, and penalize revisiting them. Without this, a greedy mover whose
-- static score has no gradient in boxed positions shuffles forever. A small
-- stack (not just the last state) breaks multi-position cycles too.
local RECENT_CAP = 48
local recent = {}        -- list of keys, newest last
local recent_set = {}    -- key -> true while in the window

-- Clear the cycle-breaker history in place (upvalues shared with remember/
-- recall). Call when a new game starts so stale history from a previous game
-- cannot distort the current one.
local function reset_history()
  recent = nil
  recent_set = nil
  recent = {}
  recent_set = {}
end

local function remember(wk, pc, bk, stm)
  local k = (wk * 64 + pc) * 64 + bk
  if recent_set[k] then
    -- move to the back (it is already remembered)
    return
  end
  recent[#recent + 1] = k
  recent_set[k] = true
  if #recent > RECENT_CAP then
    local old = table.remove(recent, 1)
    recent_set[old] = nil
  end
end

local function recall(wk, pc, bk)
  return recent_set[(wk * 64 + pc) * 64 + bk] == true
end

local function static_score(wk, pc, bk, piece)
  local edge = edge_dist[bk + 1]       -- lower: weak king closer to edge
  local kings = cheb(wk, bk)           -- lower: attacking king closer
  local mob = weak_mobility(wk, pc, bk, piece)

  local penalty = 0

  -- If the strong piece is adjacent to the weak king and not protected,
  -- it is threatened. Strongly discourage this in static evaluation.
  if cheb(pc, bk) == 1 and cheb(pc, wk) > 1 then
    penalty = 4096
  end

  -- King-approach gradient: once the weak king is confined to an edge, the
  -- dominant objective is marching the strong king in for the mate. The
  -- greedy mover otherwise prefers the boxed-but-static position forever.
  if edge == 0 then
    return penalty + mob * 128 + kings * 64 + kings * kings * 4
  end

  return penalty + mob * 128 + edge * 32 + kings * 4
end

---------------------------------------------------------------
-- Immediate mate in 1
---------------------------------------------------------------

local function find_mate_in_1(wk, pc, bk, piece)
  local moves = strong_moves(wk, pc, bk, piece)

  for _, m in ipairs(moves) do
    local replies = weak_moves(m.wk, m.pc, m.bk, piece)
    if #replies == 0 and is_check(m.wk, m.pc, m.bk, piece) then
      return m
    end
  end

  return nil
end

---------------------------------------------------------------
-- Optional tiny forced-mate search.
-- Keep max_plies small in constrained environments.
-- mate_plies = 1 is cheap.
-- mate_plies = 3 is useful but noticeably heavier.
---------------------------------------------------------------

local strong_can_mate

local function move_forces_mate(m, piece, ply)
  local replies = weak_moves(m.wk, m.pc, m.bk, piece)

  if #replies == 0 then
    return is_check(m.wk, m.pc, m.bk, piece)
  end

  if ply <= 1 then
    return false
  end

  for _, r in ipairs(replies) do
    if r.capture then
      return false
    end

    if not strong_can_mate(m.wk, m.pc, r.to, piece, ply - 2) then
      return false
    end
  end

  return true
end

strong_can_mate = function(wk, pc, bk, piece, ply)
  if ply <= 0 then
    return false
  end

  local moves = strong_moves(wk, pc, bk, piece)

  for _, m in ipairs(moves) do
    if move_forces_mate(m, piece, ply) then
      return true
    end
  end

  return false
end

local function find_forced_mate(wk, pc, bk, piece, ply)
  if ply <= 0 then
    return nil
  end

  local moves = strong_moves(wk, pc, bk, piece)

  for _, m in ipairs(moves) do
    if move_forces_mate(m, piece, ply) then
      return m
    end
  end

  return nil
end

---------------------------------------------------------------
-- Move evaluation for greedy BKM-style selection
---------------------------------------------------------------

local function evaluate_strong_move(m, piece)
  local wk, pc, bk = m.wk, m.pc, m.bk

  local replies = weak_moves(wk, pc, bk, piece)

  if #replies == 0 then
    if is_check(wk, pc, bk, piece) then
      return {
        mate = true,
        unsafe = false,
        stalemate = false,
        score = -1000000,
        mob = 0,
        edge = edge_dist[bk + 1],
        kingd = cheb(wk, bk),
        safety = cheb(pc, bk),
      }
    else
      return {
        mate = false,
        unsafe = false,
        stalemate = true,
        score = 1000000,
        mob = 0,
        edge = edge_dist[bk + 1],
        kingd = cheb(wk, bk),
        safety = cheb(pc, bk),
      }
    end
  end

  local unsafe = false
  local worst = -1000000000

  for _, r in ipairs(replies) do
    if r.capture then
      unsafe = true
      break
    end

    local sc = static_score(wk, pc, r.to, piece)
    if sc > worst then
      worst = sc
    end
  end

  if unsafe then
    return {
      mate = false,
      unsafe = true,
      stalemate = false,
      score = 900000,
      mob = #replies,
      edge = edge_dist[bk + 1],
      kingd = cheb(wk, bk),
      safety = cheb(pc, bk),
    }
  end

  return {
    mate = false,
    unsafe = false,
    stalemate = false,
    score = worst,
    mob = #replies,
    edge = edge_dist[bk + 1],
    kingd = cheb(wk, bk),
    safety = cheb(pc, bk),
  }
end

local function better_eval(a, b)
  if a.mate ~= b.mate then
    return a.mate
  end

  if a.unsafe ~= b.unsafe then
    return not a.unsafe
  end

  if a.stalemate ~= b.stalemate then
    return not a.stalemate
  end

  if a.score ~= b.score then
    return a.score < b.score
  end

  if a.mob ~= b.mob then
    return a.mob < b.mob
  end

  if a.edge ~= b.edge then
    return a.edge < b.edge
  end

  if a.kingd ~= b.kingd then
    return a.kingd < b.kingd
  end

  if a.safety ~= b.safety then
    return a.safety > b.safety
  end

  return a.key < b.key
end

---------------------------------------------------------------
-- Weak side move selection
---------------------------------------------------------------

local function best_weak_move(pos, piece)
  local wk, pc, bk = pos.wk, pos.pc, pos.bk
  local moves = weak_moves(wk, pc, bk, piece)

  if #moves == 0 then
    return nil
  end

  local best
  local best_score

  for _, m in ipairs(moves) do
    if m.capture then
      return {
        from = bk,
        to = m.to,
        piece = "K",
        capture = true,
      }
    end

    local sc = static_score(wk, pc, m.to, piece)

    -- Avoid moving into an immediate mate-in-1 if possible.
    if find_mate_in_1(wk, pc, m.to, piece) then
      sc = sc + 1000000
    end

    if not best or sc > best_score then
      best = {
        from = bk,
        to = m.to,
        piece = "K",
      }
      best_score = sc
    end
  end

  return best
end

---------------------------------------------------------------
-- Public API
---------------------------------------------------------------

function bkm.best_move(pos, piece, opts)
  opts = opts or {}
  piece = piece:upper()

  if pos.stm == 1 then
    return best_weak_move(pos, piece)
  end

  -- Immediate mate first (cheapest and always optimal).
  local m1 = find_mate_in_1(pos.wk, pos.pc, pos.bk, piece)
  if m1 then
    return m1
  end

  -- Optional small forced-mate search.
  -- Default is 1 ply, i.e. immediate mate only.
  -- Use { mate_plies = 3 } for stronger but heavier play.
  local mate_plies = opts.mate_plies or 1

  if mate_plies >= 2 then
    local m = find_forced_mate(pos.wk, pos.pc, pos.bk, piece, mate_plies)
    if m then
      return m
    end
  end

  local moves = strong_moves(pos.wk, pos.pc, pos.bk, piece)

  local best
  local best_ev

  for _, m in ipairs(moves) do
    local ev = evaluate_strong_move(m, piece)
    ev.key = m.from * 64 + m.to

    -- Cycle-breaker: strongly penalize revisiting a recently chosen state.
    if recall(m.wk, m.pc, m.bk) then
      ev.score = ev.score + 5000
    end

    if not best or better_eval(ev, best_ev) then
      best = m
      best_ev = ev
    end
  end

  -- Record the chosen state (weak to move) so a later visit is penalized.
  remember(best.wk, best.pc, best.bk, 1)

  return best
end

function bkm.score(wk, pc, bk, piece)
  return static_score(wk, pc, bk, piece:upper())
end

-- Clear the cycle-breaker history (public wrapper). Call when a new game
-- starts so stale history from a previous game cannot distort the current one.
function bkm.reset_history()
  reset_history()
end

return bkm
