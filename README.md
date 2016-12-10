# Project 5

0. Known bugs:
-


1. Parser.java is our top-level class where we read schema, records, construct query plan and write output results. Overall workflow would be:
- read a query from a file and parse it using JSqlParser
- convert the Statement obtained into a logical query plan
- convert the logical query plan into a physical query plan
- evaluate, i.e., call dump() on the physical query plan, including timing the evaluation

2. Logic for logical plan
- Build union find object to store upper bound, lower bound and equality selection information for attributes that have equality relation. To do that, we implemented a Element.java class to store the infomation for a same group of attrubutes and a UnionFind.java class to store a list of Element. We also implemented a UnionFindVisitor.java to create UnionFind object by traversing expression trees. 
- After we obtained a UnionFind object, we can use it to build a logical plan in LogicalPlanBuilder.java class. The idea is to push all relevent selection expressions to corresponding operator. Also, we kept track of residual expressions in UnionFindVisitor so that we can use them again when we build logical plan.
- When we build a logical join operator, we now add all select operators to one unique join operator. Using these children operators, we will then build left deepest tree in physical plan based on calculated cost.

3. Logic for physical plan
- We first compare the cost of using select operator to get tuples and index scan operator. We use the one with lower cost in physical plan builder.
- To determine join order, first we calculated the sizes of all base relations and store them in DBCatalog. Then, using the the sizes information and reduction factor, we implemented a JoinOrder.java class to calculate the V-value for every relations and order them from small to large. Based on this order returned by JoinOrder.class, physical plan builder will know how to construct the left deepest tree.
- To determine whether to use SMJ or BNLJ, we concluded that SMJ would outperform BNLJ when its block size is small. Therefore, we tested several block size for BNLJ and picked the one that makes SMJ significantly better than BNLJ, which is.... Also, since we can only apply SMJ for equal join relations, we will apply BNLJ on all other join expression.
