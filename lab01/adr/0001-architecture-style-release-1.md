# ADR-0001: Adopt Modular Monolith Batch Architecture for Release 1

Status: Accepted
Date: 2026-10-09 · Deciders: Chheng Kimter, Hen Chhordavattey

## Context
Angkor Mart generates monthly consolidated sales reports from branch transactions across Phnom Penh (PNH), Siem Reap (REP), and Battambang (BTB). Our architectural drivers mandate strict quality targets:
- **QA-1 (Performance)**: Process a month of approximately 3 million transaction rows ($\sim 300\text{ MB}$ to $500\text{ MB}$) on an 8-core server in $\le 60\text{ seconds}$ at 06:00 on day 2.
- **QA-3 (Availability)**: Network failures (such as REP link down for 2 hours) must not block reports for healthy branches (PNH, BTB published by 07:00), with late branch data merged automatically upon arrival in $\le 10\text{ minutes}$.
- **QA-5 (Security)**: Branch managers can strictly view only their own branch figures; unauthorized cross-branch report access must be blocked in 100% of attempts.

We operate with a small two-developer team, a tight delivery timeline, and a single dedicated server infrastructure.

## Decision
We will build Release 1 as a **Modular Monolith Batch Application** executed on a scheduled JVM process. The application is partitioned into clear, compile-time isolated packages (`model`, `ingest`, `render`) with domain invariants enforced in pure memory. Ingestion reads daily branch CSV files directly from the local file system landing zone using streaming I/O, isolates processing per branch, and exports output reports directly to destination directories.

## Alternatives considered
- **Event-Driven Microservices (Kafka / RabbitMQ + Distributed Consumers)**: Each transaction or branch file produces events processed by independent containerized services. While offering granular horizontal scaling, distributed message serialization and network round-trips make the 60-second deadline for 3 M rows (QA-1) significantly harder and introduce substantial operational overhead for a two-person team.
- **UNIX Pipe / Process Pipeline (`grep | awk | sort | custom-c-cli`)**: Extremely fast for simple filtering, but difficult to maintain for complex domain rules, multi-level currency rounding (`BigDecimal` HALF_UP), and fine-grained access control boundaries (QA-5).

## Consequences
+ **High Performance (QA-1)**: Avoids inter-process serialization, network hops, and distributed consensus, easily hitting throughput targets (>50,000 rows/s) using in-memory Java streaming on 8 cores.
+ **Operational Simplicity & Cost**: Single deployable artifact (`jar`) managed by standard system scheduling (`cron` / `systemd`) with zero distributed dependencies.
+ **Branch Isolation (QA-3)**: Package boundaries allow independent file-by-file and branch-by-branch execution loops, enabling immediate partial report output when a branch file is absent.
- **Single Point of Failure**: Hardware failure on the batch host stops the 06:00 generation run until restored or rescheduled.
- **Vertical Scaling Ceiling**: Compute capacity is bounded by the resources of the single physical/virtual server.

Revisit when: Monthly transaction volume exceeds 15 million rows or near-real-time intra-day reporting (< 5 minutes) is required by operations.
