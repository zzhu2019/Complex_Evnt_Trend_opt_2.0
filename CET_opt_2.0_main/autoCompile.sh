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
Type=CET-impl/src/util/*Type.java


Util="$DagGenTool $DS $Generator $FileParser $Type $GraphBuilder $RandomTimeGen $AnchorProcessor"

#Algos
TraversalAlgos=CET-impl/src/Traversal/*.java

#Main executor
Executor=CET-impl/src/AlgoExecutor.java
Main=CET-impl/src/Main.java



javac $Components $Util $TraversalAlgos $Executor $Main -d out/

cd out/
java Main
