# Project 5

0. Known bugs:
-


1. Parser.java is our top-level class where we read schema, records, construct query plan and write output results. Overall workflow would be:
- read a query from a file and parse it using JSqlParser
- convert the Statement obtained into a logical query plan
- convert the logical query plan into a physical query plan
- evaluate, i.e., call dump() on the physical query plan, including timing the evaluation

2. Logic for index scan operator
- To 



- To construct an instance of the IndexScanOperator, we must pass in paramenters of lowkey, highkey, lowOpen, highOpen(indicate if the range is inclusive).
- We implement a BPlusTreeDeserializer class which can deserialize the pages we need and retrieve leaf nodes(data entries).
- In IndexScanOperator.java, we handle clustered and unclustered respectively in getNextTuple(). 
If it's a clustered index, then we do an initial root-to-leaf search to retrieve the first tuple and retrieve the next tuples by scanning the sorted binary table file. If it's an unclustered index, then we do an initial root-to-leaf search to retrieve the first data entry and scan the leaf nodes left-to-right till we retrieve all the matched data entries, finally, we can retrieve actual tuples by rids stored in data entries. 
- To perform a root-to-leaf descent, we compare keys of an index node with lowkey/highkey, then go to the next level, repeat. Once we reach the leaf node level, we scan from left to right and retrieve all data entries in the specified range. In this process, only the nodes(pages) we have ever walked through are retrieved.

3. Logic for physical plan builder.
- We implement an IndexExpExtractVisitor class, which transforms the original expression to two portions, one is the lowkey/highkey part, the other is the expressions that index can't help. 
- In PhysicalPlanBuilderVisitor class, while we're translating a LogicalSelectionOperator, we use the visitor above to divide selectionExpression, then insert IndexScanOperator/SectionOperator as needed.

