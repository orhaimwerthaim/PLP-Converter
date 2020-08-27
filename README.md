# PLP-Converter
ROS-POMDP tool to convert PLPs to RDDL or to a Java simulator.

## Program arguments
* `plp_src_dir`- To specify the directory where the PLP files are at.
* `convertTo` - set `rddl` value to convert the PLPs to RDDL, else a java simulator will from created by the PLPs.
* `solver_dir` - The path to the `Solver` module directory. It is needded to know where to place the java simulator classes.


arguments examples:
1. `plp_src_dir="/home/lab/IdeaProjects/ROS-POMDP-Examples/examples/PLPs with Environment file/ICAPS experiment (for Java Simulator creation)" solver_dir="/home/lab/IdeaProjects/Solver"`
2. `plp_src_dir="/home/lab/IdeaProjects/PLP-Converter/examples/PLPs with Environment file" convertTo=rddl`