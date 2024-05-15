package com.jetbrains.rider.plugins.meadow.configurations

import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rider.model.RunnableProjectsModel
import com.jetbrains.rider.run.configurations.controls.*

class MeadowConfigurationViewModel(val projectSelector: ProjectSelector,
                                private val runnableProjectsModel: RunnableProjectsModel?,
                                private val lifetime: Lifetime
) : RunConfigurationViewModelBase() {

    override val controls: List<ControlBase>
        get() = listOf(projectSelector)

    private var isLoaded = false
    private val type = MeadowConfigurationType()

    init {
        disable()
        runnableProjectsModel?.projects?.adviseOnce(lifetime) {
            runnableProjectsModel.projects.view(lifetime) { projectListLt, projectList ->
                projectSelector.projectList.addAll(projectList.filter { type.isApplicable(it.kind) }.sortedBy { it.fullName })
                projectListLt.onTermination {
                    projectSelector.projectList.clear()
                }
            }
            enable()
        }
    }

    fun reset(projectFilePath: String) {
        isLoaded = false
        runnableProjectsModel?.projects?.adviseOnce(lifetime) { projectList ->
            if (projectFilePath.isEmpty()) {
                projectList.firstOrNull { type.isApplicable(it.kind) }?.let { project ->
                    projectSelector.project.set(project)
                    isLoaded = true
                }
            }
            else {
                projectList.singleOrNull { it.projectFilePath == projectFilePath }?.let { project ->
                    projectSelector.project.set(project)
                }
            }
            isLoaded = true
        }
    }

}