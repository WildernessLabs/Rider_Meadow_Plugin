namespace Meadow;

public static class MeadowPortUtil
{
    private const int BasePort = 55898;

    private static int _nextPortCounter;
    
    public static int GetNextDebuggingPort()
    {
        var shift = _nextPortCounter++;
        if (_nextPortCounter > 100)
        {
            _nextPortCounter = 0;
        }

        return BasePort + shift;
    }
}