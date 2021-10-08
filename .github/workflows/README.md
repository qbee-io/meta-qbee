### A Simple workflow for building meta-qbee layer against Poky hardknott

This workflow was created to be run on a selfhosted Linux machine.
It will fetch this repository, clone poky, add qbee-layer and run the build.

#### Adding a self-hosted runner

Navigate to repository Settings -> Actions -> Runners, then click on the green button to add a new self-hosted runner.
Choose Linux as the runner image, then follow the instructions. (On a windows machine, follow the instructions in WSL2)

#### Starting the workflow

To start the workflow navigate to Actions in the top bar, select 'Qbee Layer - Selfhost' workflow then click on 'Run workflow'

The workflow takes around 1h 15min to complete on a Laptop with 16GB of RAM and 6/12 cores/threads Intel i7-8750H.
<br><br>**Please note** that you will need atleast 50 GB of space to run the build.
