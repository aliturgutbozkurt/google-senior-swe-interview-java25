# SPEC-01: Spec-Driven Development (SDD) & Google L5/L6 Interview Framework

## 1. Executive Summary

At Google, candidates for **Senior Software Engineer (L5)** and **Staff Software Engineer (L6)** are evaluated not merely on whether they can produce code that passes LeetCode-style test cases, but on how they operate under ambiguity, structure complex problem domains, maintain rigorous invariants, and design extensible, robust systems.

**Spec-Driven Development (SDD)** is the professional methodology that distinguishes senior and staff engineers from junior/mid-level candidates. In an interview setting, SDD shifts the dynamic from passive test-taking to proactive technical collaboration.

---

## 2. The 6-Phase SDD Interview Protocol

```
+--------------------------------------------------------------------------+
|                       Phase 1: Ambiguity & Clarification                 |
|  - Define functional boundaries, scale, inputs/outputs, edge constraints |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       Phase 2: Formal Specification                      |
|  - Establish Mathematical Invariants, Pre/Post-conditions, Signatures    |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       Phase 3: Complexity & Resource Budget              |
|  - Derive strict Time & Space limits (amortized, worst-case, cache lines)|
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       Phase 4: Test Contract Formulation                 |
|  - Golden path, Null/Empty, Boundary Extremes, Degenerate & Stress tests  |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       Phase 5: Idiomatic Implementation                  |
|  - Modern Java 25 (Records, Pattern Matching, Sealed Types, Struct-Conc) |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       Phase 6: Verification & Senior Follow-ups          |
|  - Dry run with trace table, concurrency implications, distributed scale  |
+--------------------------------------------------------------------------+
```

### Phase 1: Clarification & Boundary Analysis (Minutes 0 - 5)
Never write code immediately. Ask targeted, architectural questions:
1. **Data Dimensions & Scale**: What is the maximum value of $N$ (number of elements)? What are memory constraints ($10^5 \implies O(N \log N)$, $10^7 \implies O(N)$, $10^9 \implies$ streaming/external memory)?
2. **Data Characteristics**: Are inputs sorted? Can values be negative? Are duplicates permitted? Can values overflow 32-bit signed integers?
3. **Mutability & Concurrency**: Is this an in-memory data structure accessed by multiple concurrent threads? Is read-heavy or write-heavy?
4. **Failure Modes**: How should illegal arguments be treated? Return empty `Optional`, throw `IllegalArgumentException`, or return sentinel?

### Phase 2: Formal Technical Specification (Minutes 5 - 10)
Formalize the interface and invariants before implementation:
- **State Invariants**: What must ALWAYS be true before and after any public operation? (e.g., in a Segment Tree, `tree[i] = tree[2*i] + tree[2*i+1]`).
- **Pre-conditions**: What guarantees must the caller uphold?
- **Post-conditions**: What guarantees does the algorithm provide upon termination?
- **Interface Definition**: Write clean Java 25 interfaces or records.

### Phase 3: Complexity & Resource Budget (Minutes 10 - 12)
State and justify asymptotic bounds upfront:
- Time: Average case, amortized, and absolute worst-case.
- Space: Auxiliary memory, recursion stack depth, GC pressure (avoiding unnecessary allocations).

### Phase 4: Test Contracts & Edge Matrix (Minutes 12 - 15)
Define test categories:
1. **Base Case / Minimal Input**: Empty input ($N=0$), single element ($N=1$), identical elements.
2. **Boundary Extremes**: `Integer.MIN_VALUE`, `Integer.MAX_VALUE`, floating point precision, massive sparse inputs.
3. **Degenerate Structures**: Trees reduced to linked lists (skewed trees), dense graphs ($E = V^2$), cyclic graphs.
4. **Adversarial / Stress Cases**: Worst-case inputs designed to trigger hash collisions or $O(N^2)$ quicksort degradation.

### Phase 5: Production-Grade Implementation (Minutes 15 - 35)
Write clean, modular, self-documenting code using modern Java 25 idioms:
- Strong typing, avoiding raw types.
- Immutable data structures via records where applicable.
- Defensive copying and encapsulation of internal arrays.
- Eliminating magic numbers with named constants.

### Phase 6: Systematic Verification & Follow-up (Minutes 35 - 45)
1. Dry-run code using a clear pointer / variable trace table on a non-trivial example.
2. Proactively bring up trade-offs:
   - "If memory is constrained, we could replace pointers with flat primitive arrays."
   - "If this needs to handle concurrent writers, we can switch from `synchronized` to fine-grained striping or lock-free atomics."

---

## 3. Google Senior SWE (L5/L6) Assessment Rubric

Google evaluation committees score candidates across 4 core signals:

| Evaluation Signal | What L3/L4 (Junior/Mid) Demonstrates | What L5/L6 (Senior/Staff) Must Demonstrate |
| :--- | :--- | :--- |
| **General Cognitive Ability (GCA)** | Finds *a* working solution with hints; struggles when assumptions change. | Rapidly models unknown problem spaces, decomposes complexity, synthesizes multiple optimal approaches, weighs trade-offs proactively. |
| **Role-Related Knowledge (RRK)** | Writes standard Java; knows basic collections (`ArrayList`, `HashMap`). | Deep mastery of Java memory model, data cache locality, bitwise manipulations, concurrent hazards (false sharing, ABA, CAS loops), algorithmic optimality. |
| **Leadership** | Waits for step-by-step instructions from interviewer. | Drives the interview session like a senior tech lead driving an RFC review. Clarifies scope, structures time, handles ambiguity without hesitation. |
| **Googleyness** | Defensive when questioned; seeks easy shortcuts. | Collaborative, receptive to feedback, intellectual humility, prioritizes long-term correctness and engineering excellence over hacks. |

---

## 4. Think-Out-Loud Communication Protocols

Senior candidates maintain an unbroken channel of structured communication:
1. **The Intent Signal**: Always state *why* you are choosing a specific data structure before writing it.
   > *"I am choosing an Indexed Priority Queue over a standard `PriorityQueue` because we need $O(\log V)$ decrease-key operations for Dijkstra, avoiding $O(E \log E)$ stale-key bloat."*
2. **The Bottleneck Identification**: Point out the primary performance or memory choke point.
   > *"The naive approach requires $O(N \cdot K)$ recalculations. The bottleneck is finding the minimum in an overlapping sliding window. We can eliminate this with a monotonic deque to achieve $O(N)$ amortized time."*
3. **The Pivot Strategy**: If stuck or if an edge case invalidates your approach, explain the invalidation logically before pivoting.
