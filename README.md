# Project 3

1. Parser.java is our top-level class where we read schema, records, construct query plan and write output results. Overall workflow would be:
- read a query from a file and parse it using JSqlParser
- convert the Statement obtained into a logical query plan
- convert the logical query plan into a physical query plan
- evaluate, i.e., call dump() on the physical query plan, including timing the evaluation

2. Working Directory
- Logical operators: src/edu/cornell/cs4321/LogicalOperators/
- Physical operators: src/edu/cornell/cs4321/PhysicalOperators/
- PhysicalPlanBuilder: src/edu/cornell/cs4321/Visitors/PhysicalPlanBuilderVisitor
