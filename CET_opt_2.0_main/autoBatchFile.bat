@echo off
:: This is the auto-compile file written for CET-opt project
set "Components=CET-impl/src/Components/*.java"

:: utils classes
set "DagGenTool=CET-impl/src/util/dagGen/*.java"
set "DS=CET-impl/src/util/CustomDS/*.java"
set "Generator=CET-impl/src/util/GraphGenerator.java"
set "FileParser=CET-impl/src/util/FileGraphParser.java"
set "GraphBuilder=CET-impl/src/util/GraphBuilder.java"
set "RandomTimeGen=CET-impl/src/util/RandomTimeGenerator.java"
set "AnchorProcessor=CET-impl/src/util/AnchorProcessor.java"
set "Type=CET-impl/src/util/*Type.java"

set "Util=%DagGenTool% %DS% %Generator% %FileParser% %GraphBuilder% %RandomTimeGen% %AnchorProcessor% %Type%"

:: Algos
set "TraversalAlgos=CET-impl/src/Traversal/*.java"

:: Main & Executor
set "Executor=CET-impl/src/AlgoExecutor.java"
set "Main=CET-impl/src/Main.java"

javac %Components% %Util% %TraversalAlgos% %Executor% %Main% -d out/
cd out/
java Main
cd ..

