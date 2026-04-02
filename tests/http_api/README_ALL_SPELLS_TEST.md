# All Spells Test Suite

Comprehensive testing infrastructure for testing all spells across all hero classes in Remixed Dungeon.

## Files

- `test_all_spells.py` - Main Python test script
- `run_all_spells_test.sh` - Bash wrapper script with auto-start functionality
- `game_client.py` - Base client library (shared with other tests)

## Quick Start

### Option 1: Using the wrapper script (recommended)

```bash
# Test all spells for all classes
./run_all_spells_test.sh

# Test specific class
./run_all_spells_test.sh --class DOCTOR

# Test specific spell
./run_all_spells_test.sh --spell BloodTransfusion

# Use different port
./run_all_spells_test.sh --port 8082
```

The wrapper script will automatically detect if the server is running and offer to start it.

### Option 2: Direct Python script

```bash
# Start the game with webserver first
./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer

# Then run tests
python3 test_all_spells.py
```

## Usage

### Python Script Options

```
python3 test_all_spells.py [--port PORT] [--host HOST] [--class CLASS] [--spell SPELL] [--verbose] [--json]
```

| Option | Description | Default |
|--------|-------------|---------|
| `--host` | WebServer host | localhost |
| `--port` | WebServer port | 8080 |
| `--class` | Test specific hero class only | All classes |
| `--spell` | Test specific spell only | All spells |
| `--verbose`, `-v` | Verbose output | False |
| `--json` | Output results as JSON | False |

### Examples

```bash
# Test all spells for all 9 hero classes
python3 test_all_spells.py

# Test only Doctor class spells
python3 test_all_spells.py --class DOCTOR

# Test only the BloodTransfusion spell
python3 test_all_spells.py --spell BloodTransfusion

# Test with verbose output
python3 test_all_spells.py -v

# Output results as JSON for CI/CD integration
python3 test_all_spells.py --json > results.json

# Use custom port
python3 test_all_spells.py --port 8082
```

## Hero Classes and Spells

The script tests spells for all 9 hero classes:

| Class | Affinity | Spells |
|-------|----------|--------|
| WARRIOR | Combat | DieHard, Dash, BodyArmor, Smash |
| MAGE | Witchcraft | Roar, LightningBolt, Heal, Order |
| ROGUE | Rogue | Cloak, Backstab, KunaiThrow, Haste |
| HUNTRESS | Huntress | Calm, Charm, ShootInEye, SummonBeast |
| ELF | Elf | MagicArrow, Sprout, HideInGrass, NatureArmor |
| NECROMANCER | Necromancy | RaiseDead, Exhumation, DarkSacrifice, Possess |
| GNOLL | Common | TownPortal, Heal, RaiseDead, Cloak, Calm, Charm |
| PRIEST | Priest | Heal, Calm, Charm, Order |
| DOCTOR | PlagueDoctor | Anesthesia, Heal, BloodTransfusion, CorpseExplosion |

## Test Output

### Console Output

The script provides detailed console output:

```
======================================================================
Testing DOCTOR spells
======================================================================
Starting game with DOCTOR...
  Hero class: DOCTOR
  Magic affinity: PlagueDoctor
  Available spells: 4
  Spells to test: Anesthesia, Heal, BloodTransfusion, CorpseExplosion

  Testing Anesthesia... ✓ Spell cast successfully (0.52s)
  Testing Heal... ✓ Spell cast successfully (0.51s)
  Testing BloodTransfusion... ✓ Expected behavior: No valid target (0.53s)
  Testing CorpseExplosion... ✓ Expected behavior: NoCorpse (0.54s)
```

### Summary Report

```
======================================================================
TEST SUMMARY
======================================================================

Total: 34/36 tests passed (94.4%)
Failed: 2/36

Results by Hero Class:
----------------------------------------------------------------------
  WARRIOR        4/4 (100.0%)
  MAGE           4/4 (100.0%)
  ROGUE          4/4 (100.0%)
  HUNTRESS       3/4 ( 75.0%)
    ✗ SummonBeast: No valid target
  ELF            4/4 (100.0%)
  NECROMANCER    4/4 (100.0%)
  GNOLL          6/6 (100.0%)
  PRIEST         4/4 (100.0%)
  DOCTOR         4/4 (100.0%)

Results by Spell:
----------------------------------------------------------------------
  ✓ Anesthesia                 1/1 (100.0%)
  ✓ Backstab                   1/1 (100.0%)
  ✓ BloodTransfusion           1/1 (100.0%)
  ⚠ SummonBeast                0/1 (  0.0%)
  ...
```

### JSON Output

For CI/CD integration:

```bash
python3 test_all_spells.py --json > results.json
```

```json
[
  {
    "hero_class": "DOCTOR",
    "spell": "Anesthesia",
    "success": true,
    "message": "Spell cast successfully",
    "duration": 0.52
  },
  {
    "hero_class": "DOCTOR",
    "spell": "Heal",
    "success": true,
    "message": "Spell cast successfully",
    "duration": 0.51
  }
]
```

## Expected Behaviors

Some spells may appear to "fail" but are actually working correctly:

| Spell | Expected Behavior | Reason |
|-------|------------------|--------|
| BloodTransfusion | "No valid target" | Requires nearby mob with life essence |
| CorpseExplosion | "NoCorpse" | Requires Carcass item nearby |
| SummonBeast | "No valid target" | Requires valid spawn location |
| Backstab | "No valid target" | Requires unsuspecting target behind |
| KunaiThrow | "No valid target" | Requires target in range |

The script recognizes these as expected behaviors and marks them as passing tests.

## Prerequisites

1. **Java 10+** and **Gradle 8.x**
2. **Python 3.6+** with dependencies:
   ```bash
   pip3 install requests
   ```

## Starting the WebServer

### Method 1: Helper Script

```bash
./start_game_server.sh [--port PORT]
```

### Method 2: Gradle

```bash
./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=8080 --windowed"
```

### Method 3: Standalone Server

```bash
./gradlew -p RemixedDungeonDesktop runWebServer --args="--webserver=8080 --mod=Remixed"
```

## Integration with CI/CD

The test suite can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions step
- name: Start game server
  run: |
    ./tests/http_api/start_game_server.sh &
    sleep 10

- name: Run spell tests
  run: |
    cd tests/http_api
    python3 test_all_spells.py --json > results.json
    exit_code=$?
    
- name: Upload test results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: tests/http_api/results.json
```

## Troubleshooting

### Server not responding

```bash
# Check if server is running
curl http://localhost:8080/ready

# Check game logs for errors
curl http://localhost:8080/log
```

### Spell tests timing out

Increase the timeout in the script or reduce the number of spells tested:

```bash
# Test single class first
python3 test_all_spells.py --class DOCTOR
```

### Permission denied (shell script)

```bash
chmod +x run_all_spells_test.sh
```

## Related Files

- `test_doctor_spells.py` - Doctor-specific spell tests (predecessor)
- `test_blood_transfusion.py` - Single spell test example
- `game_client.py` - Base HTTP client library
- `DebugEndpoints.java` - Server-side debug API implementation

## See Also

- [WebServer Documentation](../../docs/WebServer.md)
- [DebugEndpoints Documentation](../../docs/DebugEndpoints.md)
- [Doctor Spell Testing](../../docs/DoctorSpellTesting.md)
