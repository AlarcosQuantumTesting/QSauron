package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.HashMap;
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

    public List<Map<String,String>> getProjectsName(Map<String, String> request) throws Exception {

    List<Map<String,String>> projects = new ArrayList<Map<String,String>>();

    try {
        alarideProxy.validateToken(request);
    } catch (Exception e) {
        throw new Exception("Error validating token: " + e.getMessage(), e);
    }
    String userId = request.get("email");
    List<Object> p = projectRepository.findProjectsNameByUserId(userId);

    for (int i = 0; i < p.size(); i ++) {

        Map<String,String> m = new HashMap<>();
        Object o = p.get(i);
        
        Object[] arr = (Object[]) o;
        String id = String.valueOf(arr[0]);
        String name = String.valueOf(arr[1]);
        Object generator = arr[2];
        String type = generator.getClass().getName();
        m.put("id", id);
        m.put("name", name);
        m.put("type", type);
        projects.add(m);
    }


    return projects;
    }

    public Project getProject(Map<String, String> request) throws Exception {

    try {
        alarideProxy.validateToken(request);
    } catch (Exception e) {
        throw new Exception("Error validating token: " + e.getMessage(), e);
    }

    String userId = request.get("email");

    User user = userRepository.getById(userId);

    String projectId = request.get("projectId");
    
    Project project = projectRepository.getById(projectId);

    if(!project.getUsers().contains(user)){
        throw new Exception("Usuario no asociado al proyecto: " + userId);
    }


    return project;
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
