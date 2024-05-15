using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ProjectModel;
using JetBrains.Rider.Model;

namespace MeadowPlugin;

[ZoneMarker]
public class ZoneMarker : IRequire<IProjectModelZone>, IRequire<IRiderModelZone>;