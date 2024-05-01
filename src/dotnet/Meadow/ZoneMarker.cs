using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ReSharper.Features.Running;

namespace Meadow;

[ZoneMarker]
public class ZoneMarker : IRequire<RunnableProjectsZone>;