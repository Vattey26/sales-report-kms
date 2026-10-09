## Context diagram
The diagram deliberately does not show the internal structure of the system, the file formats, or the finance director as a person (covered by the e-mail service).
The system as a whole serves QA-1 (the report is ready on the morning of day 2) and QA-5 (managers see only their own branch).
Open question for the client: do branch managers need the report on a, or only on a head-office network?

## Container diagram
The diagram deliberately does not show classes, packages, the BigDecimal money rule, or how many threads the engine uses.
The Report batch serves QA-1 (4 M rows in 60s or less) and the landing zone wiht the Ingest jod serves QA-3 (a late REP file does not delay the other branches.)
Open question for the client: does head office already have a login system(SSO) for the branch managers, or must we create and maintain accounts ourselves (QA-5)?