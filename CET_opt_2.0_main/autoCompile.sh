#Componenets
Components=CET-impl/src/Components/*.java

#utils
DagGenTool=CET-impl/src/util/dagGen/*.java
DS=CET-impl/src/util/CustomDS/*.java
Processor=CET-impl/src/util/GraphProcessor.java
Generator=CET-impl/src/util/GraphGenerator.java
FileParser=CET-impl/src/util/FileGraphParser.java
GraphBuilder=CET-impl/src/util/GraphBuilder.java
RandomTimeGen=CET-impl/src/util/RandomTimeGenerator.java
AnchorProcessor=CET-impl/src/util/AnchorProcessor.java
DAGFactory=CET-impl/src/util/DAGFactory.java
Type=CET-impl/src/util/*Type.java


Util="$DagGenTool $DS $Generator $FileParser $Type $GraphBuilder $RandomTimeGen $AnchorProcessor $DAGFactory"

#Algos
TraversalAlgos=CET-impl/src/Traversal/*.java
TraversalConcurrentTasks=CET-impl/src/Traversal/ConcurrentRunnables/*.java

#Main executor
Main=CET-impl/src/Main.java



javac $Components $Util $TraversalAlgos $TraversalConcurrentTasks $Executor $Main -d out/

cd out/
java src.Main
