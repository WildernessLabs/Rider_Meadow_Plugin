# Phase 1: DAP Centralization - Incremental Implementation Plan

**Goal:** Consolidate to single DAP code path while maintaining getSerialPorts functionality

**Testing Checkpoint After EACH Step:**
```powershell
.\gradlew buildPlugin
.\gradlew runIde
# Open Meadow solution → Test device selector populates
# Check logs for "MeadowBackendHost" initialization
```

---

## Step 1: Delete Legacy Mono Debugger Files ✅

**Files to Delete:**
- `src/rider/main/kotlin/.../MeadowDebugProfileState.kt`
- `src/rider/main/kotlin/.../MeadowRunProfileState.kt`

**Why Safe:** 
- These are Kotlin frontend files
- Don't affect C# backend or MeadowBackendHost instantiation
- No protocol/RD dependencies

**Test:** getSerialPorts should still work (no backend changes)

---

## Step 2: Create MeadowDebugPortProvider ✅

**Create:**
- `src/rider/main/kotlin/.../MeadowDebugPortProvider.kt`
- Centralizes port generation (moved from deleted MeadowDebugProfileState)

**Modify:**
- `MeadowConfiguration.kt` - use new provider

**Why Safe:**
- Pure Kotlin refactor
- No backend changes
- No protocol changes

**Test:** getSerialPorts should still work

---

## Step 3: Update DAP Adapter Reference ✅

**Modify:**
- `MeadowDebugAdapterSupportProvider.kt` - Fix exe name
- `build.gradle.kts` - Update DAP source path

**Why Safe:**
- Only affects DAP launch, not device discovery
- Backend still untouched

**Test:** getSerialPorts should still work

---

## Step 4: Delete AppRunSession (CRITICAL - Backend Change) ⚠️

**Delete:**
- `src/dotnet/MeadowPlugin/AppRunSession.cs`
- `src/rider/main/kotlin/.../MeadowAppProcessHandler.kt`

**Modify:**
- `MeadowBackendHost.cs` - Remove AppRunSession tracking/registration

**DANGER ZONE:**
This modifies the C# backend component that has `[SolutionComponent]` DI.

**What to Change in MeadowBackendHost.cs:**
1. Remove `_runSessions` dictionary
2. Keep `GetSerialPortsAsync` method UNTOUCHED
3. Simplify `RegisterAppSessionAsync` to remove AppRunSession creation
4. Change `DropSessionForPort` to no-op

**Critical Rule:** DO NOT touch logger initialization or GetSerialPortsAsync!

**Test:** 
- getSerialPorts MUST still work
- Device selector MUST populate
- Backend logs MUST show MeadowBackendHost instantiation

---

## Step 5: Update Protocol Model (Backend Impact) ⚠️

**Modify:**
- `MeadowPluginModel.kt` - Clean up unused RD signals

**Why Risky:**
- Protocol regeneration affects C# generated code
- Could break RD bindings

**Safe Approach:**
- Only remove `runSessions` map
- Keep `getSerialPorts` call UNCHANGED
- Keep `appOutput` sink (may still be used)

**Test:** getSerialPorts must work after protocol regeneration

---

## Testing Checklist (After Each Step)

```powershell
# 1. Clean build
.\gradlew clean buildPlugin

# 2. Launch sandbox
.\gradlew runIde

# 3. In sandbox Rider:
#    - Open Bluetooth_Basics solution
#    - Wait for indexing to complete
#    - Click device selector dropdown (top toolbar next to "main")
#    - Verify "Updating list of devices..." completes
#    - Verify COM11 device appears

# 4. Check logs
Get-Content "build\idea-sandbox\RD-2025.2\log\idea.log" | Select-String "MeadowBackendHost|GetSerialPorts"

# Expected:
# - "MeadowBackendHost created" (or similar instantiation log)
# - "GetSerialPortsAsync invoked" when dropdown opens
# - "Found X serial ports"
```

---

## Rollback Plan (If Any Step Breaks getSerialPorts)

```powershell
# Rollback last change
git checkout HEAD~1

# Rebuild
.\gradlew clean buildPlugin

# Test again
.\gradlew runIde
```

---

## What We Learned from Previous Failure

**Root Cause:** In commit `852a471`, `MeadowBackendHost.cs` referenced deleted class in logger:
```csharp
private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();
using MeadowPlugin.Deployment;  // Both deleted!
```

This prevented `[SolutionComponent]` from instantiating the class.

**Prevention:**
- After deleting any C# file, grep for references:
  ```powershell
  Get-ChildItem -Recurse -Filter "*.cs" | Select-String "ClassNameHere"
  ```
- Test backend instantiation after every backend C# change
- Add constructor logging early to verify DI activation

---

## Success Criteria for Phase 1

✅ All legacy Mono debugger files deleted
✅ Single DAP code path for both Debug and Run
✅ AppRunSession removed (output via DAP only)
✅ getSerialPorts still works perfectly
✅ Device selector populates
✅ Backend logs show MeadowBackendHost active
✅ Can proceed to Phase 2 (progress reporting)
