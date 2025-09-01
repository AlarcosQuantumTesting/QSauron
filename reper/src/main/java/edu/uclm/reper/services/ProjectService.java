package edu.uclm.reper.services;

import edu.uclm.reper.http.AlarideProxy;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarideProxy alarideProxy;

     @Autowired
     private MutantCycleRepository mutantCycleRepository;


    // Crea un nuevo proyecto y lo asocia con un usuario
    public Project createProjectForUser(String projectName, String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        User user = userOpt.get();

        Project project = new Project(projectName);
        
        if (project.getUsers() == null) {
            project.setUsers(new ArrayList<>());
        }
        
        project.getUsers().add(user);

        return projectRepository.save(project);
    }

    //Asocia un proyecto existente con un usuario
    public void associateProjectWithUser(String projectId, String userId) {
        
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Proyecto no encontrado con ID: " + projectId);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        Project project = projectOpt.get();
        User user = userOpt.get();

        if (project.getUsers() == null) {
            project.setUsers(new ArrayList<>());
        }

        if (!project.getUsers().contains(user)) {
            project.getUsers().add(user);
            projectRepository.save(project);
        }

    }
    

    //Obtiene todos los proyectos de un usuario
    public List<Project> getProjectsByUser(Map<String, String> request) throws Exception {

        try {
            alarideProxy.validateToken(request);
        } catch (Exception e) {
            throw new Exception("Error validating token: " + e.getMessage(), e);
        }
        String userId = request.get("email");
        return projectRepository.findProjectsByUserId(userId);
    }

    
    /*//Remueve la asociación entre un proyecto y un usuario
    public Project removeUserFromProject(String projectId, String userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Proyecto no encontrado con ID: " + projectId);
        }
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        Project project = projectOpt.get();
        User user = userOpt.get();

        if (project.getUsers() != null) {
            project.getUsers().remove(user);
            return projectRepository.save(project);
        }

        return project;
    }*/


    public Project saveProject(Project projectData) {
        projectRepository.save(projectData);
        if(projectData.getMutantCycles() != null && !projectData.getMutantCycles().isEmpty()){
            List<MutantCycle> mtnc = projectData.getMutantCycles();
            saveMutantCycles(projectData, mtnc);
        }
        
        return projectData;
    
    }

    private void saveMutantCycles(Project projectData, List<MutantCycle> mutantCycle) {
       
            for (MutantCycle cycle : mutantCycle) {
                cycle.setProject(projectData);
                mutantCycleRepository.save(cycle);
                saveMutants(cycle);
            }
             
    }

    private void saveMutants(MutantCycle mutantCycle) {
    
        mutantCycle.getMutants().forEach(mutant -> mutant.setMutantCycle(mutantCycle));
    }

   

}
