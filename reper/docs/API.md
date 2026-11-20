# reper REST API

Base URL (local): `http://localhost:8080`. Request bodies are JSON. Authentication uses the `SESSION_TOKEN` HttpOnly cookie set by `POST /users/login`; most endpoints also expect the caller's `email` in the body, which must match the token. A failed check returns `401`.

**Access legend:** *Public* = no session; *Session* = valid `SESSION_TOKEN` cookie; *Admin* = session of an administrator.

## Users — `/users`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/users/create` | Public | Register a user |
| POST | `/users/login` | Public | Log in; sets session and refresh cookies (or requests 2FA) |
| POST | `/users/getUser` | Cookie (checked in service) | Current user profile |
| POST | `/users/activate2FA` | Login flow (not checked by `validateCookie`) | Enable two-factor authentication |
| POST | `/users/verify2FACode` | Public | Verify a 2FA code |
| POST | `/users/refreshToken` | Refresh cookie | Renew the session token |
| POST | `/users/logout` | Cookie (not checked by `validateCookie`) | Close the session |

## Password recovery — `/recovery`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/recovery/request` | Public | Send a recovery code by e-mail |
| POST | `/recovery/validate` | Public | Validate the code |
| POST | `/recovery/reset` | Public | Set a new password |

## Projects — `/projects`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/projects/create` | Session | Create a project |
| POST | `/projects/associate` | Session | Associate a project with a user |
| POST | `/projects/getAllByUser` | Session | List the user's projects |
| POST | `/projects/getAllByUserSummary` | Session | Lightweight project list |
| POST | `/projects/getProject` | Session | Get one project |
| POST | `/projects/getProjectDetails` | Session | Project details |
| PUT | `/projects/save` | Session | Save a project |
| POST | `/projects/delete` | Session | Delete a project |

## Project import — `/projects/import`
| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/projects/import/formats` | Public | Supported import formats |
| POST | `/projects/import` | Session | Import one or more circuit files as projects |
| POST | `/projects/import/convert` | Session | Convert a file without importing it |
| POST | `/projects/import/represent` | Session | Build a visual representation of a circuit |

## Experiments — `/experiments`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/experiments/create` | Session | Create and start an experiment (mutant generation + execution) |
| POST | `/experiments/active-progress` | Session | Progress of running experiments |
| POST | `/experiments/user` | Session | List the user's experiments |
| POST | `/experiments/details` | Session | Experiment details |
| POST | `/experiments/delete` | Session | Delete an experiment |
| POST | `/experiments/resume-preview` | Session | Preview what resuming would re-run |
| POST | `/experiments/resume` | Session | Resume a failed experiment |
| POST | `/experiments/merge-preview` | Session | Preview merging experiments |
| POST | `/experiments/merge` | Session | Merge experiments |

## Background jobs — `/jobs`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/jobs/status` | Session | Status of a background job |
| POST | `/jobs/result` | Session | Result of a finished job |

## Mutation data (QuMu) — `/qumureper`
| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/qumureper/getoperatorsByFamily` | Public | Mutation operators grouped by family |
| POST | `/qumureper/getSidebarProjects` | Session | Projects for the side bar |
| POST | `/qumureper/saveMutantsBatch` | Session | Persist a batch of mutants |
| POST | `/qumureper/saveExecutionsBatch` | Session | Persist a batch of execution results |
| POST | `/qumureper/deleteMutantCycle` | Session | Delete a mutant cycle |
| POST | `/qumureper/getProjectLight` | Session | Lightweight project data |
| POST | `/qumureper/getCycleMatrix` | Session | Killing matrix of a cycle |
| POST | `/qumureper/getMutant` | Session | One mutant |
| POST | `/qumureper/getMutantCircuits` | Session | Circuits of mutants |

## Generators (QuCo) — `/qucoreper`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/qucoreper/getcircuit` | none in controller | Get a generator by id |
| POST | `/qucoreper/savecircuit` | none in controller | Save a generator |
| POST | `/qucoreper/getProjectsName` | Session | Project names |

> `getcircuit` and `savecircuit` do not call the session check in the controller; review before exposing publicly.

## Statistics — `/mutantanalytics`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/mutantanalytics/cycleStatistics/start` | Session | Start statistics computation for a cycle (background job) |
| POST | `/mutantanalytics/calculateStatistics` | Session | Compute statistics |
| POST | `/mutantanalytics/uploadResults` | Session | Forward results to `mutant_analytics` |
| POST | `/mutantanalytics/chatQuestion` | Session | Ask a question about the results |

## Converter — `/converter`
| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/converter/formats` | Public | Format catalogue (cached, from q-convert-api) |
| GET | `/converter/health` | Public | Converter health |
| POST | `/converter/convert` | Admin | Convert circuit code between formats |
