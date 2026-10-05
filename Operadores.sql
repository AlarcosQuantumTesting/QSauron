INSERT INTO qsauron.operator_types (id, name) VALUES
(2, 'Control Gates'),
(1, 'Initialization errors'),
(5, 'Other operators'),
(4, 'Position errors'),
(3, 'Swap Gates'),
(6, 'Phase Gates');

INSERT INTO qsauron.operators 
(id, description, enabled, name, operator_type_id) 
VALUES
(1, 'Initialization error that changes the qubit''s initial value', 1, 'ChangeInitialValue', 1),
(2, 'Initialization error that duplicates the circuit''s first gate', 1, 'FirstGateDuplication', 1),
(3, 'Swaps H gates with X gates', 1, 'H_X', 3),
(4, 'Swaps H gates with Y gates', 1, 'H_Y', 3),
(5, 'Swaps H gates with Z gates', 1, 'H_Z', 3),
(6, 'Swaps X gates with H gates', 1, 'X_H', 3),
(7, 'Swaps X gates with Y gates', 1, 'X_Y', 3),
(8, 'Swaps X gates with Z gates', 1, 'X_Z', 3),
(9, 'Swaps Y gates with X gates', 1, 'Y_X', 3),
(10, 'Swaps Y gates with H gates', 1, 'Y_H', 3),
(11, 'Swaps Y gates with Z gates', 1, 'Y_Z', 3),
(12, 'Swaps Z gates with H gates', 1, 'Z_H', 3),
(13, 'Swaps Z gates with X gates', 1, 'Z_X', 3),
(14, 'Swaps Z gates with Y gates', 1, 'Z_Y', 3),
(15, 'Duplicates an additional gate in the circuit', 1, 'FurtherGateDuplication', 5),
(16, 'Removes uncontrolled control gates', 1, 'NonControlGateRemoval', 5),
(17, 'Moves a gate down to the adjacent position', 1, 'MoveGateDownAdj', 4),
(18, 'Moves a gate down to all possible positions', 1, 'MoveGateDownAll', 4),
(19, 'Moves a gate up to the adjacent position', 1, 'MoveGateUpAdj', 4),
(20, 'Moves a gate up to all possible positions', 1, 'MoveGateUpAll', 4),
(21, 'Moves the controlled qubit downward', 1, 'ChangeControlledQubitDown', 4),
(22, 'Moves the controlled qubit upward', 1, 'ChangeControlledQubitUp', 4),
(23, 'Moves the control qubit downward', 1, 'ChangeControlQubitDown', 4),
(24, 'Moves the control qubit upward', 1, 'ChangeControlQubitUp', 4),
(25, 'Removes control gates from the circuit', 1, 'ControlGateRemoval', 2),
(26, 'Swaps control and controlled qubits', 1, 'SwapControlAndControlledQubits', 2),
(27, 'Swaps Z gates with Z^½ gates', 1, 'Z_Z^½', 6),
(28, 'Swaps Z gates with Z^¼ gates', 1, 'Z_Z^¼', 6),
(30, 'Swaps Z^½ gates with Z^¼ gates', 1, 'Z^½_Z^¼', 6),
(29, 'Swaps Z^½ gates with Z gates', 1, 'Z^½_Z', 6),
(32, 'Swaps Z^¼ gates with Z^½ gates', 1, 'Z^¼_Z^½', 6),
(31, 'Swaps Z^¼ gates with Z gates', 1, 'Z^¼_Z', 6);

SET NAMES utf8mb4;

-- 1) Nuevas familias
INSERT INTO qsauron.operator_types (name) VALUES
('Noise insertion'),
('Gate addition');

-- 2) Operadores de inserción de ruido (3)
INSERT INTO qsauron.operators (description, enabled, name, operator_type_id)
SELECT v.description, v.enabled, v.name, t.id FROM (
  SELECT 'Inserts a small Rx(0.1) noise rotation at the beginning of the circuit, on each qubit' AS description, 1 AS enabled, 'NoiseInsertionStart' AS name
  UNION ALL SELECT 'Inserts a small Rx(0.1) noise rotation at the end of the circuit, on each qubit', 1, 'NoiseInsertionEnd'
  UNION ALL SELECT 'Inserts a small Rx(0.1) noise rotation at a random position of the circuit, on each qubit', 1, 'NoiseInsertionRandom'
) v JOIN qsauron.operator_types t ON t.name = 'Noise insertion';

-- 3) Operadores de agregación de puertas (3 posiciones x 16 puertas = 48)
INSERT INTO qsauron.operators (description, enabled, name, operator_type_id)
SELECT v.description, v.enabled, v.name, t.id FROM (
  SELECT 'Adds an H gate at the beginning of the circuit, on each qubit' AS description, 1 AS enabled, 'AddGateStart_H' AS name
  UNION ALL SELECT 'Adds an X gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_X'
  UNION ALL SELECT 'Adds an X^½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_X^½'
  UNION ALL SELECT 'Adds an X^-½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_X^-½'
  UNION ALL SELECT 'Adds an X^¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_X^¼'
  UNION ALL SELECT 'Adds an X^-¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_X^-¼'
  UNION ALL SELECT 'Adds a Y gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Y'
  UNION ALL SELECT 'Adds a Y^½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Y^½'
  UNION ALL SELECT 'Adds a Y^-½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Y^-½'
  UNION ALL SELECT 'Adds a Y^¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Y^¼'
  UNION ALL SELECT 'Adds a Y^-¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Y^-¼'
  UNION ALL SELECT 'Adds a Z gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Z'
  UNION ALL SELECT 'Adds a Z^½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Z^½'
  UNION ALL SELECT 'Adds a Z^-½ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Z^-½'
  UNION ALL SELECT 'Adds a Z^¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Z^¼'
  UNION ALL SELECT 'Adds a Z^-¼ gate at the beginning of the circuit, on each qubit', 1, 'AddGateStart_Z^-¼'
  UNION ALL SELECT 'Adds an H gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_H'
  UNION ALL SELECT 'Adds an X gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_X'
  UNION ALL SELECT 'Adds an X^½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_X^½'
  UNION ALL SELECT 'Adds an X^-½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_X^-½'
  UNION ALL SELECT 'Adds an X^¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_X^¼'
  UNION ALL SELECT 'Adds an X^-¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_X^-¼'
  UNION ALL SELECT 'Adds a Y gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Y'
  UNION ALL SELECT 'Adds a Y^½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Y^½'
  UNION ALL SELECT 'Adds a Y^-½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Y^-½'
  UNION ALL SELECT 'Adds a Y^¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Y^¼'
  UNION ALL SELECT 'Adds a Y^-¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Y^-¼'
  UNION ALL SELECT 'Adds a Z gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Z'
  UNION ALL SELECT 'Adds a Z^½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Z^½'
  UNION ALL SELECT 'Adds a Z^-½ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Z^-½'
  UNION ALL SELECT 'Adds a Z^¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Z^¼'
  UNION ALL SELECT 'Adds a Z^-¼ gate at the end of the circuit, on each qubit', 1, 'AddGateEnd_Z^-¼'
  UNION ALL SELECT 'Adds an H gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_H'
  UNION ALL SELECT 'Adds an X gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_X'
  UNION ALL SELECT 'Adds an X^½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_X^½'
  UNION ALL SELECT 'Adds an X^-½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_X^-½'
  UNION ALL SELECT 'Adds an X^¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_X^¼'
  UNION ALL SELECT 'Adds an X^-¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_X^-¼'
  UNION ALL SELECT 'Adds a Y gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Y'
  UNION ALL SELECT 'Adds a Y^½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Y^½'
  UNION ALL SELECT 'Adds a Y^-½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Y^-½'
  UNION ALL SELECT 'Adds a Y^¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Y^¼'
  UNION ALL SELECT 'Adds a Y^-¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Y^-¼'
  UNION ALL SELECT 'Adds a Z gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Z'
  UNION ALL SELECT 'Adds a Z^½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Z^½'
  UNION ALL SELECT 'Adds a Z^-½ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Z^-½'
  UNION ALL SELECT 'Adds a Z^¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Z^¼'
  UNION ALL SELECT 'Adds a Z^-¼ gate at a random position of the circuit, on each qubit', 1, 'AddGateRandom_Z^-¼'
) v JOIN qsauron.operator_types t ON t.name = 'Gate addition';

SET NAMES utf8mb4;
UPDATE qsauron.operators SET description = 'Inserts the configured noise rotations (axis and angle) as a new column at the beginning of the circuit, on each qubit' WHERE name = 'NoiseInsertionStart';
UPDATE qsauron.operators SET description = 'Inserts the configured noise rotations (axis and angle) as a new column at the end of the circuit, on each qubit' WHERE name = 'NoiseInsertionEnd';
UPDATE qsauron.operators SET description = 'Inserts the configured noise rotations (axis and angle) as a new column at a random position of the circuit, on each qubit' WHERE name = 'NoiseInsertionRandom';