# Strassen-Algorithm

Run like 

`java App.java <input_filename.txt> <ouput_filename.txt>`

Some summary is printed but most output will be written to output.txt

There is a sample output attached as "LabHashingOutput.txt" as well as outputs from larger datasets. 

Developed on java 11 with vm args: <br> 
-XX:+UseParallelGC <br> 
-XX:GCTimeRatio=4 <br> 
-XX:AdaptiveSizePolicyWeight=90 <br> 
-Dsun.zip.disableMemoryMapping=true <br> 
-Xmx2G -Xms100m
