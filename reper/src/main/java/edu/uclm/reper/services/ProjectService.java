package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.http.AlarideProxy;
import edu.uclm.reper.model.Mutant;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.Operator;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.MutantCycleRepository;
import edu.uclm.reper.repository.OperatorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.QProgramRepository;
import edu.uclm.reper.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarideProxy alarideProxy;

    @Autowired
    private MutantCycleRepository mutantCycleRepository;

    @Autowired
    private QProgramRepository qProgramRepository;

    @Autowired
    private OperatorRepository operatorRepository;


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
        List<Project> projects = projectRepository.findProjectsByUserId(userId);
        for (Project project : projects) {
            project.getMutantCycles(); // Evita la serialización de usuarios para cada proyecto
        }
        return projects;
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
        
        if(projectData.getMutantCycles() != null && !projectData.getMutantCycles().isEmpty()){
            List<MutantCycle> mtnc = projectData.getMutantCycles();
            for(int i = 0; i < mtnc.size(); i++){
                MutantCycle cycle = mtnc.get(i);
                saveMutantsQPrograms(cycle.getMutants());
            }
        }
        projectRepository.save(projectData);
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

    private void saveMutantsQPrograms(List<Mutant> mutants) {
        QProgram quirkCircuit = null;
        for(int i = 0; i < mutants.size(); i++) {
            Mutant mutant = mutants.get(i);
            if (mutant.getQuirkCircuit() != null) {
                quirkCircuit = mutant.getQuirkCircuit();
                qProgramRepository.save(quirkCircuit);
            }
            if(mutant.getOperator().getName() != null ){

                Optional<Operator> operator = operatorRepository.findByName(mutant.getOperator().getName());

                if(operator.isPresent()){
                    mutant.setOperator(operator.get());
                } 
            
            }
        }
    }
   

}
