package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.run.RiderRunBundle
import com.jetbrains.rider.run.configurations.ProtocolLifetimedSettingsEditor
import com.jetbrains.rider.run.configurations.controls.ControlViewBuilder
import com.jetbrains.rider.run.configurations.controls.ProjectSelector
import com.jetbrains.rider.run.configurations.runnableProjectsModelIfAvailable
import javax.swing.JComponent

class MeadowConfigurationEditor(private val project: Project) : ProtocolLifetimedSettingsEditor<MeadowConfiguration>() {

    private lateinit var viewModel: MeadowConfigurationViewModel

    override fun resetEditorFrom(c: MeadowConfiguration) {
        val p = c.parameters
        viewModel.reset(p.projectFilePath)
    }

    override fun applyEditorTo(c: MeadowConfiguration) {
        val selectedProject = viewModel.projectSelector.project.valueOrNull
        if (selectedProject != null) {
            val p = c.parameters
            p.projectFilePath = selectedProject.projectFilePath
        }
    }

    override fun createEditor(lifetime: Lifetime): JComponent {
        viewModel = MeadowConfigurationViewModel(
            ProjectSelector(RiderRunBundle.message("label.project.with.colon"), "Project"),
            project.runnableProjectsModelIfAvailable,
            lifetime)
        return ControlViewBuilder(lifetime, project, MeadowConfigurationType.RUN_CONFIG_ID).build(viewModel)
    }
}