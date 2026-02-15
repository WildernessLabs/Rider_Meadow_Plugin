package com.jetbrains.rider.plugins.meadow.dap

import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XBreakpointType

class MeadowExceptionBreakpointType : XBreakpointType<XBreakpoint<XBreakpointProperties<*>>, XBreakpointProperties<*>>(
    "MeadowExceptionBreakpoint",
    "Meadow Exception Breakpoint"
) {
    override fun getDisplayText(breakpoint: XBreakpoint<XBreakpointProperties<*>>): String =
        "Meadow Exception"
}
