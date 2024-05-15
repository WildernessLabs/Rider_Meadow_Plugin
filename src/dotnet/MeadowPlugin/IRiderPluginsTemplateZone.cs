using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ProjectModel;

namespace MeadowPlugin;

[ZoneMarker]
public class ZoneMarker : IRequire<IProjectModelZone>;