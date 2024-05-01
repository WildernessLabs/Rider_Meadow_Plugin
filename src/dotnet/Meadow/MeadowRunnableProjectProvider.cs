using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Features.Running;
using JetBrains.Rider.Model;
using JetBrains.Util;

namespace Meadow;

[SolutionComponent]
public class MeadowRunnableProjectProvider : IRunnableProjectProvider
{
    private const string MeadowOsSdkPrefix = "Meadow.Sdk";
    private static readonly RunnableProjectKind OurProjectKind = new("Meadow");
    
    public RunnableProject? CreateRunnableProject(IProject project, string name, string fullName, IconModel? icon)
    {
        var sdk = project.ProjectProperties.DotNetCorePlatform?.Sdk;
        if (sdk == null) return null;
        if (!sdk.StartsWith(MeadowOsSdkPrefix, StringComparison.OrdinalIgnoreCase)) return null;
        
        
        var projectOutputs = project.TargetFrameworkIds.Where(x=>x.IsNetStandard).Select(tfm =>
            ProjectOutputBuilder.Create(
                tfm.ToRdTargetFrameworkInfo(),
                project.GetOutputFilePath(tfm).NormalizeSeparators(FileSystemPathEx.SeparatorStyle.Unix),
                [],
                project.GetOutputFilePath(tfm).Directory.NormalizeSeparators(FileSystemPathEx.SeparatorStyle.Unix))).ToList();
        
        return new RunnableProject(
            name,
            fullName,
            project.ProjectFileLocation.NormalizeSeparators(FileSystemPathEx.SeparatorStyle.Unix),
            OurProjectKind,
            projectOutputs,
            [],
            null,
            []);
    }

    public IEnumerable<RunnableProjectKind> HiddenRunnableProjectKinds { get; } =
        new[] {   
            CommonRunnableProjectKinds.StaticMethod
        };
}