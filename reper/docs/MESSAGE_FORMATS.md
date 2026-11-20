# Generator message formats

JSON accepted for each circuit-generation strategy. The `type` field selects the strategy. The `Generator` class in `reper/src/main/java/edu/uclm/reper/model/Generator.java` registers the types `GRENOBLE`, `MATRIX`, `GENETIC`, `GROVER`, `BLOCKS` and `EDITOR`.

## GRENOBLE
```json
{
  "type": "GRENOBLE",
  "interestingRows": 3,
  "physicalAngle": 45.0,
  "parallel": true,
  "splitted": false
}
```

## MATRIX
```json
{
  "type": "MATRIX",
  "interestingRows": 4,
  "positionValue": { "0": 1, "1": 2, "2": 5, "3": 7 }
}
```

## GENETIC
```json
{
  "type": "GENETIC",
  "hadamards": [true, false, true, false],
  "minColumns": 2,
  "maxColumns": 10,
  "initPopSize": 50,
  "maxPopSize": 200,
  "desiredError": 0.001,
  "gates": ["H", "CNOT", "X", "Z", "RY"]
}
```

## GROVER
```json
{
  "type": "GROVER",
  "truePositions": [1, 3, 7, 15]
}
```

## GROVER_RUDOLPH
Documented in the original message-format notes; it is not registered in `Generator.java`, so verify it before relying on it.
```json
{
  "type": "GROVER_RUDOLPH",
  "positionValue": { "0": 5, "1": 2, "2": 8, "3": 1, "4": 9 }
}
```
