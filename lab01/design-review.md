# Software Design & Architecture Review: Angkor Mart Sales Report

## 1. Package Responsibilities (Cohesion)

- **`edu.itc.salesreport` (Application Root)**: Serves as the application composition root that bootstraps the runtime environment, binds concrete dependencies, and triggers batch report execution.
- **`edu.itc.salesreport.model` (Domain Model)**: Encapsulates pure domain representations, immutable entities, summary records, and fundamental validation invariants for monthly sales reporting.
- **`edu.itc.salesreport.ingest` (Ingestion & ETL)**: Coordinates scanning, parsing, validating daily branch CSV transactions, and broadcasting ingestion progress events to subscribed observers.
- **`edu.itc.salesreport.render` (Presentation & Export)**: Transforms aggregated monthly report models into concrete presentation formats (text, HTML, CSV) via format-specific strategy implementations.

---

## 2. Package Coupling & Instability Metrics

Coupling is measured from package import declarations within `src/main/java/edu/itc/salesreport`:
- **Afferent Coupling ($C_a$)**: Number of external project packages that depend upon classes within this package.
- **Efferent Coupling ($C_e$)**: Number of external project packages upon which classes in this package depend.
- **Instability ($I$)**: Metric defined by Robert C. Martin:
  $$I = \frac{C_e}{C_a + C_e}$$
  Range: $0$ (maximally stable, difficult to change) to $1$ (maximally unstable, easily changeable).

| Package | Responsibility | $C_a$ | $C_e$ | Instability Formula & Arithmetic | Instability ($I$) |
|---|---|:---:|:---:|---|:---:|
| `edu.itc.salesreport.model` | Core domain entities & invariants | 2 | 0 | $I = \frac{0}{2 + 0} = \frac{0}{2}$ | **0.00** (Maximally Stable) |
| `edu.itc.salesreport.ingest` | CSV parsing & progress notification | 1 | 1 | $I = \frac{1}{1 + 1} = \frac{1}{2}$ | **0.50** (Balanced) |
| `edu.itc.salesreport.render` | Report format strategies | 0 | 1 | $I = \frac{1}{0 + 1} = \frac{1}{1}$ | **1.00** (Maximally Unstable) |
| `edu.itc.salesreport` | Composition root & execution entry point | 0 | 1 | $I = \frac{1}{0 + 1} = \frac{1}{1}$ | **1.00** (Maximally Unstable) |

*Notes on couplings:*
- `model` is imported by `ingest` (uses `SaleTransaction`, `PaymentMethod`) and `render` (uses `MonthlyReport`, `BranchSummary`), while importing 0 internal packages.
- `ingest` is imported by root `edu.itc.salesreport` (uses `MonthLoader`, `ConsoleProgress`, `CsvTransactionParser`) and depends on `model`.
- `render` depends on `model` and has 0 dependents in main source.
- Root depends on `ingest` and has 0 dependents.

---

## 3. Coupling Accepted and Justification

**Accepted Coupling**: Both `edu.itc.salesreport.ingest -> edu.itc.salesreport.model` and `edu.itc.salesreport.render -> edu.itc.salesreport.model`.

**Architectural Rationale**:
In accordance with the Stable Dependencies Principle (SDP) and Clean Architecture, dependencies must always point in the direction of stability ($I \to 0$). The `model` package represents the core domain ubiquitous language (immutable records `SaleTransaction`, `MonthlyReport`, `BranchSummary`) with zero outgoing dependencies ($I = 0.00$). Both ingestion and rendering naturally require domain types to exchange data. Crucially, as enforced by `DependencyRulesTest`, `ingest` has zero knowledge of `render` ($C_e = 0$ between them), ensuring that changing or adding renderers never ripples into file loading or data aggregation.

---

## 4. Architectural Trade-off for QA-4: Sealed Hierarchy vs. Open Interface

**Scenario QA-4 (Modifiability)**:
> *"A new export format (for example XLSX) is requested next to the existing ones: new renderer is added behind the renderer interface; aggregation code is untouched ($\le 2$ person-days, $\le 3$ files changed, $\le 150$ lines changed, 0 files changed in aggregation)."*

### Comparison:

1. **Open Interface (`public interface ReportRenderer`)**:
   - *Strengths*: Strictly conforms to the Open-Closed Principle (OCP). A developer can add `XlsxReportRenderer` in a separate class or even another package without touching `ReportRenderer.java`.
   - *Weaknesses*: Lacks compile-time exhaustiveness checking. Consumers handling renderers must rely on runtime type checks (`instanceof`) or fall-through `default` cases, increasing bug risk when format-specific handling or UI menu mapping is needed.

2. **Sealed Interface (`public sealed interface ReportRenderer permits ...`)**:
   - *Strengths*: Enables compiler-enforced pattern-matching completeness (e.g. `switch (renderer)`) without fallback `default` branches, catching missing format handlers at build time across CLI options, UI selectors, and dispatch tables.
   - *QA-4 Impact*: Adding a new renderer requires editing the `permits` clause in `ReportRenderer.java` (+1 file, +1 line) and updating the factory `forExtension` (+3 lines). This requires creating 1 new file (`XlsxReportRenderer.java`), modifying 1 file (`ReportRenderer.java`), and 1 test file (`ReportRendererTest.java`)—totalling **3 files changed and $\sim 60$ lines**, which easily satisfies QA-4's budget ($\le 3$ files, $\le 150$ lines, 0 aggregation changes).

### Conclusion:
For Release 1 of this internal batch system, the **sealed interface hierarchy better serves QA-4**. The tiny overhead of adding one token to the `permits` clause is vastly outweighed by the compiler guarantees against unhandled formats across the system.
