# SPEC-05: Googleyness, Leadership & L5/L6 Behavioral Mastery

## 1. Google Evaluation Pillars for Senior Engineers

At Google, candidates at L5 (Senior Software Engineer) and L6 (Staff Software Engineer) undergo a dedicated **Googleyness & Leadership** interview. The committee expects proof of past behavior demonstrating leadership without authority, intellectual honesty, navigating ambiguous requirements, and building psychological safety.

---

## 2. Core Behavioral Attributes

### 2.1 Thriving in Ambiguity
- **Expectation**: Senior engineers are handed vague business directives (e.g., *"Make our service 30% faster"* or *"Figure out our multi-region data strategy"*).
- **Signal**: Candidate does not freeze or complain about lack of specs. Instead, they formulate hypotheses, interview stakeholders, establish telemetry/metrics, write an RFC, and rally consensus.

### 2.2 Doing the Right Thing (Googleyness)
- **Expectation**: Putting user interest and team health ahead of short-term vanity metrics or personal ego.
- **Signal**: Speaking up respectfully when an architectural shortcut endangers user privacy or system reliability; admitting mistakes openly; fostering inclusive discussions.

### 2.3 Leading Without Authority & Driving Consensus
- **Expectation**: L5/L6 engineers influence across multiple teams without being people managers.
- **Signal**: When senior engineers have irreconcilable technical disagreements, they do not pull rank or enter flame wars. They anchor decisions in empirical data, POCs, trade-off matrices, and the "Disagree and Commit" principle.

### 2.4 Blameless Culture & Psychological Safety
- **Expectation**: Failures and outages are seen as opportunities to fix systems, not to blame humans.
- **Signal**: Demonstrating how past production incidents led to automated safeguards, comprehensive canarying, and blameless post-mortems.

---

## 3. The STAR-L Formula (Senior Engineering Edition)

Google interviewers prefer the structured **STAR** approach augmented with **Learning & Systemic Impact (STAR-L)**:

```
+---------------------------------------------------------------------------------+
| Situation: High-stakes context, technical complexity, business stakes (15%)     |
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Task: Exact challenge, ambiguity, and your specific role / ownership (15%)      |
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Action: Concrete technical and leadership steps YOU drove. Trade-offs weighed.  |
|         How you mobilized consensus, mitigated risks, and unblocked others (50%)|
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Result: Measurable metrics (latency, QPS, cost, uptime) + Organizational impact |
|         + What you learned / What you would do differently in retrospect (20%)   |
+---------------------------------------------------------------------------------+
```

---

## 4. Top L5/L6 Behavioral Scenarios & Answer Architectures

### Scenario 1: High-Stakes Technical Disagreement
- **Question**: *"Tell me about a time you strongly disagreed with a senior colleague or architect on a critical design decision. How was it resolved?"*
- **Senior Strategy**:
  - Do not portray the other engineer as incompetent.
  - Show how you formulated both proposals into a clear trade-off matrix evaluated against agreed business goals (e.g., write-latency vs read-consistency).
  - Built a lightweight prototype/benchmark to test the core assumption.
  - If the benchmark proved your hypothesis, won them over with facts. If it proved their hypothesis, gracefully conceded and championed their design.

### Scenario 2: Severe Production Outage & Post-Mortem
- **Question**: *"Describe the worst production incident you caused or led the resolution for."*
- **Senior Strategy**:
  - Focus on immediate mitigation (rollback, traffic shedding, circuit breaking) before root cause diagnosis.
  - Take personal accountability.
  - Detail the Blameless Post-Mortem: 5 Whys, timeline, root cause (e.g., cascading retry storm on cache degradation).
  - Detail systemic remediations: Exponential backoff with full jitter, circuit breakers, synthetic canary monitoring.

### Scenario 3: Navigating Massive Technical Debt
- **Question**: *"How do you balance pushing for clean architecture and paying down technical debt against urgent feature delivery from product managers?"*
- **Senior Strategy**:
  - Frame tech debt in terms product managers care about: developer velocity, mean time to recovery (MTTR), cloud infrastructure costs, and incident frequency.
  - Introduce the "Tech Debt Tax" or 20% continuous allocation per sprint.
  - Break massive refactorings into incremental Strangler Fig migrations with zero downtime.
