# ADR-0002: Daily Branch-to-Head-Office Data Push with Idempotent Recovery

Status: Accepted
Date: 2026-10-09 · Deciders: Chheng Kimter, Hen Chhordavattey

## Context
Each day, POS terminals at branches (PNH, REP, BTB) produce hundreds of thousands of transactions. Head office requires consolidated monthly reporting starting at 06:00 on the second day of each month.
Key forces and scenario requirements:
- **QA-3 (Availability & Fault Tolerance)**: Unstable branch network connections (e.g., REP offline for 2 hours) must not delay available branch reports (PNH and BTB must publish by 07:00). When missing branch data arrives, the full consolidated chain report must regenerate automatically in $\le 10\text{ minutes}$.
- **QA-1 (Performance)**: Ingestion of daily files must be batched and streamable to complete within the 60-second limit.
- **Data Integrity & Idempotency**: Re-transmissions, re-runs, and network retries must never cause duplicate transaction totals or corrupted sums.

## Decision
We will use an **outbound daily file push over secure HTTPS / SFTP** from each branch to the head-office landing zone (`data/yyyy-MM/`):
1. **Branch Export**: At local store closing (23:00 daily), each branch generates a single daily file named `<BRANCH>-<yyyy-MM-dd>.csv` accompanied by a `<BRANCH>-<yyyy-MM-dd>.csv.sha256` checksum file, and uploads both to the head-office landing zone via HTTPS POST.
2. **Failure & Partial Report Policy (QA-3)**:
   - At the scheduled 06:00 run, head office checks for the presence of all required branch daily files.
   - **Timeout / Grace Period**: Head office waits up to 15 minutes (until 06:15) for any lagging branch transfers. If a branch file (e.g. REP) is still missing at 06:15, head office marks that branch as `PENDING` and immediately generates partial branch reports for all healthy branches (PNH, BTB) by 06:30, satisfying QA-3 well ahead of 07:00.
   - **Late File Merge**: A file system watcher daemon on the landing zone monitors for late-arriving files. When REP's file arrives (e.g. at 08:00), the watcher triggers an incremental re-run that loads the late branch data and publishes the completed consolidated chain report within 10 minutes.
   - **Idempotency Guarantee**: Batch ingestion keys on the tuple `(branch, date, receipt_no, sku)`. Re-uploaded daily files overwrite existing files in the landing zone and replace prior staged rows rather than appending, guaranteeing that re-runs are strictly idempotent.

## Alternatives considered
- **Head Office Pulls via SSH/API from Each Branch**: Head office initiates connections to branch servers. Rejected because branches operate behind dynamic NATs, residential fiber, and retail firewalls without static public IPs, making inbound connections fragile and complex to secure.
- **Per-Receipt Event Streaming to Message Broker (MQTT / Cloud Pub/Sub)**: POS publishes each receipt individually over WAN. Rejected because unstable store internet links cause message buffering, backpressure drops, and complex out-of-order deduplication overhead for release 1.

## Consequences
+ **Fault Isolation & Partial Reporting (QA-3)**: A network failure at one branch never impacts the ingestion or publication schedule of another branch.
+ **Guaranteed Idempotency**: Fixed daily file naming (`BRANCH-yyyy-MM-dd.csv`) and SHA-256 verification enable safe and automated retries.
+ **Bandwidth Efficiency**: A single compressed/streamed CSV file transfer per day is resilient to intermittent link dropouts (can resume or re-upload).
- **Batch Latency**: Intra-day sales are not visible to head office until the overnight upload completes.
- **Storage Management**: Landing zone disk retention and cleanup policies must be maintained to prune processed archive files.

Revisit when: Business requires real-time intra-day inventory tracking across branches or if store connections upgrade to leased direct fiber lines.
