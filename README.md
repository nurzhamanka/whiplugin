## Wound Healing Tool
For my **Senior Project** at Nazarbayev University, I decided to build a tool for performing batch image segmentation of microscope images obtained during a [wound healing assay](https://en.wikipedia.org/wiki/Wound_healing_assay). The tool is actually a plugin for [FIJI/ImageJ2](https://fiji.sc/), an open-source Java image processing program. My friend, *Sholpan Kauanova, a Ph.D. student at Nazarbayev University*, inspired me to develop this. The project is built using the tools from the [SciJava](https://scijava.org/) ecosystem.  
![wound healing tool art](img/woundhealing.png)  
**The plugin consists of two parts:** *the base tool* for processing a dataset or a set of datasets using a predefined algorithm with some tunable parameters, and *a GUI* allowing users to create graphs representing custom algorithms.

### Installation
- Download [the latest release](https://github.com/nurzhamanka/whiplugin/releases)
- Install via FIJI (Plugins > Install Plugin...)

### Usage
The plugin can process datasets (stacks) loaded in memory. It can also process datasets on disk if they are organized in a tree structure:
```bash
root/ (select this directory)  
├── dataset01/  
│   ├── image01  
│   ├── image02  
│   ├── image03  
│   └── ...  
├── dataset02/  
│   ├── image01  
│   └── ...  
└── dataset03/  
    ├── image01  
    └── ...  
```
For those wanting to experiment with the segmentation routine, there is an experimental GUI for building a custom pipeline. Users can build graphs, where each node represents one parametrized operation on the input image. The GUI then finds all paths from the input node to the output node and evaluates each. It is built with JGraphX and JGraphT. It is still quite raw, so expect bugs.
