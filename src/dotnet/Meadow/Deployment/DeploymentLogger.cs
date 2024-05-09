using JetBrains.Rider.Model;

namespace Meadow.Deployment;

public class DeploymentLogger(DeploymentSession deploymentSession)
{
    public void OnOutputAvailable(string output)
    {
        deploymentSession.OutputAdded(new OutputMessage(output, DeployMessageKind.Info));
    }
    
    public void OnErrorAvailable(string error)
    {
        deploymentSession.OutputAdded(new OutputMessage(error, DeployMessageKind.Error));
    }
}