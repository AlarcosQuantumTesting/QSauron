package edu.uclm.reper.services;

import edu.uclm.reper.http.AlarideProxy;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.User;
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
    public Project associateProjectWithUser(Long projectId, String userId) {
        
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
            return projectRepository.save(project);
        }

        return project;
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

    
    //Remueve la asociación entre un proyecto y un usuario
    public Project removeUserFromProject(Long projectId, String userId) {
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
    }

}
