package com.jetbrains.rider.plugins.meadow.dap

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase

class MeadowLineBreakpointType : XLineBreakpointTypeBase(
    "MeadowLineBreakpoint",
    "Meadow Line Breakpoint",
    null
) {
    override fun canPutAt(file: VirtualFile, line: Int, project: Project): Boolean {
        return file.extension == "cs"
    }
}
