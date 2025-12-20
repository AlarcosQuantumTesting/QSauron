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
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.MutantRepository;
import edu.uclm.reper.repository.OperatorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.QCircuitRepository;
import edu.uclm.reper.repository.QProgramRepository;
import edu.uclm.reper.repository.UserRepository;
import edu.uclm.reper.repository.MutantCycleRepository;
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
    private QProgramRepository qProgramRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private QCircuitRepository qCircuitRepository;

    @Autowired
    private MutantRepository mutantRepository;

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

    // Asocia un proyecto existente con un usuario
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

    // Obtiene todos los proyectos de un usuario
    public List<Project> getProjectsByUser(Map<String, String> request) throws Exception {

        try {
            alarideProxy.validateToken(request);
        } catch (Exception e) {
            throw new Exception("Error validating token: " + e.getMessage(), e);
        }
        String userId = request.get("email");
        List<Project> projects = projectRepository.findProjectsByUserId(userId);
        for (Project project : projects) {
            project.getMutantCycles();
        }
        return projects;
    }

    public List<Map<String, String>> getProjectsName(Map<String, String> request) throws Exception {

        List<Map<String, String>> projects = new ArrayList<Map<String, String>>();

        try {
            alarideProxy.validateToken(request);
        } catch (Exception e) {
            throw new Exception("Error validating token: " + e.getMessage(), e);
        }
        String userId = request.get("email");
        List<Object> p = projectRepository.findProjectsNameByUserId(userId);

        for (int i = 0; i < p.size(); i++) {

            Map<String, String> m = new HashMap<>();
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

        User user = userRepository.findById(userId).get();

        String projectId = request.get("projectId");

        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            throw new Exception("Proyecto no encontrado con ID: " + projectId);
        }

        Project project = projectOpt.get();

        if (!project.getUsers().contains(user)) {
            throw new Exception("Usuario no asociado al proyecto: " + userId);
        }

        return project;
    }

    public boolean deleteProject(String projectId) {
        try {
            projectRepository.deleteById(projectId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Project saveProject(Project projectData) {
        // 1. If new project (no ID), save directly but ensure relationships
        if (projectData.getId() == null || projectData.getId().isEmpty()) {
            prepareNewProject(projectData);
            return projectRepository.save(projectData);
        }

        // 2. If existing project, merge manually
        Optional<Project> existingOpt = projectRepository.findById(projectData.getId());
        if (existingOpt.isEmpty()) {
            prepareNewProject(projectData);
            return projectRepository.save(projectData);
        }

        Project existingProject = existingOpt.get();
        updateProjectFields(existingProject, projectData);

        return projectRepository.save(existingProject);
    }

    private void prepareNewProject(Project project) {
        // Ensure main project circuit is managed
        if (project.getqProgram() != null) {
            prepareQProgram(project.getqProgram());
        }

        if (project.getMutantCycles() != null) {
            for (MutantCycle mc : project.getMutantCycles()) {
                mc.setProject(project);
                if (mc.getMutants() != null) {
                    saveMutantsQPrograms(mc.getMutants());
                    for (Mutant m : mc.getMutants()) {
                        m.setMutantCycle(mc);
                    }
                }
            }
        }
    }

    private void updateProjectFields(Project existing, Project incoming) {
        existing.setName(incoming.getName());

        // Update QProgram
        if (incoming.getqProgram() != null) {
            QProgram incomingProgram = incoming.getqProgram();
            prepareQProgram(incomingProgram); // Fix QCircuit reference before attaching

            // Save/Merge QProgram explicitly and update reference
            // This ensures 'existing' refers to a managed QProgram, preventing duplicate
            // entry on cascade
            QProgram savedProgram = qProgramRepository.save(incomingProgram);
            existing.setqProgram(savedProgram);
        }

        // Update MutantCycles
        updateMutantCycles(existing, incoming.getMutantCycles());

        // Update Users
        if (incoming.getUsers() != null) {
            if (existing.getUsers() == null)
                existing.setUsers(new ArrayList<>());
            existing.setUsers(incoming.getUsers());
        }

        // Update Notes
        if (incoming.getNotes() != null) {
            if (existing.getNotes() == null)
                existing.setNotes(new ArrayList<>());
            existing.getNotes().clear();
            existing.getNotes().addAll(incoming.getNotes());
        }
    }

    private void updateMutantCycles(Project existingProject, List<MutantCycle> newCycles) {
        if (newCycles == null)
            return;

        List<MutantCycle> currentCycles = existingProject.getMutantCycles();
        if (currentCycles == null) {
            currentCycles = new ArrayList<>();
            existingProject.setMutantCycles(currentCycles);
        }

        Map<Integer, MutantCycle> currentMap = new HashMap<>();
        for (MutantCycle mc : currentCycles) {
            currentMap.put(mc.getId(), mc);
        }

        List<MutantCycle> toKeep = new ArrayList<>();

        for (MutantCycle newMc : newCycles) {
            saveMutantsQPrograms(newMc.getMutants());

            if (newMc.getId() != 0 && currentMap.containsKey(newMc.getId())) {
                MutantCycle existingMc = currentMap.get(newMc.getId());
                existingMc.setDate(newMc.getDate());

                if (newMc.getExecConfig() != null) {
                    newMc.getExecConfig().setMutantCycle(existingMc);
                    existingMc.setExecConfig(newMc.getExecConfig());
                }

                updateMutants(existingMc, newMc.getMutants());
                toKeep.add(existingMc);
            } else {
                MutantCycle cycleToUse = newMc;
                // If ID is not 0, try to find it in DB to avoid multiple representations
                if (newMc.getId() != 0) {
                    Optional<MutantCycle> dbCycle = mutantCycleRepository.findById(newMc.getId());
                    if (dbCycle.isPresent()) {
                        cycleToUse = dbCycle.get();
                        cycleToUse.setDate(newMc.getDate());
                        if (newMc.getExecConfig() != null) {
                            newMc.getExecConfig().setMutantCycle(cycleToUse);
                            cycleToUse.setExecConfig(newMc.getExecConfig());
                        }
                        updateMutants(cycleToUse, newMc.getMutants());
                    }
                }

                if (cycleToUse == newMc) {
                    // Was not found in DB or ID was 0
                    newMc.setProject(existingProject);
                    if (newMc.getMutants() != null) {
                        for (Mutant m : newMc.getMutants()) {
                            m.setMutantCycle(newMc);
                        }
                        updateMutants(newMc, newMc.getMutants());
                    }
                } else {
                    // Found in DB, ensure project link
                    cycleToUse.setProject(existingProject);
                }
                toKeep.add(cycleToUse);
            }
        }

        currentCycles.clear();
        currentCycles.addAll(toKeep);
    }

    private void updateMutants(MutantCycle existingCycle, List<Mutant> newMutants) {
        if (newMutants == null)
            return;

        List<Mutant> currentMutants = existingCycle.getMutants();
        if (currentMutants == null) {
            currentMutants = new ArrayList<>();
            existingCycle.setMutants(currentMutants);
        }

        Map<Integer, Mutant> currentMap = new HashMap<>();
        Map<String, Mutant> currentMapByQProgram = new HashMap<>();

        for (Mutant m : currentMutants) {
            currentMap.put(m.getId(), m);
            if (m.getQuirkCircuit() != null && m.getQuirkCircuit().getId() != null) {
                currentMapByQProgram.put(m.getQuirkCircuit().getId(), m);
            }
        }

        List<Mutant> toKeep = new ArrayList<>();

        for (Mutant newM : newMutants) {
            Mutant existingM = null;

            // 1. Try to find existing mutant in the current cycle by ID
            if (newM.getId() != 0 && currentMap.containsKey(newM.getId())) {
                existingM = currentMap.get(newM.getId());
            }
            // 2. Try to find existing mutant in the current cycle by QProgram ID
            else if (newM.getQuirkCircuit() != null && newM.getQuirkCircuit().getId() != null
                    && currentMapByQProgram.containsKey(newM.getQuirkCircuit().getId())) {
                existingM = currentMapByQProgram.get(newM.getQuirkCircuit().getId());
            }
            // 3. Try to find existing mutant GLOBALLY by QProgram ID (to avoid UK
            // violation)
            else if (newM.getQuirkCircuit() != null && newM.getQuirkCircuit().getId() != null) {
                List<Mutant> found = mutantRepository.findByQuirkCircuitId(newM.getQuirkCircuit().getId().trim());
                if (!found.isEmpty()) {
                    existingM = found.get(0);
                }
            }

            if (existingM != null) {
                existingM.setIndex(newM.getIndex());
                existingM.setColumn(newM.getColumn());
                existingM.setRow(newM.getRow());
                existingM.setQuirkCircuit(newM.getQuirkCircuit());
                existingM.setOperator(newM.getOperator());

                // Ensure it belongs to the current cycle (moving it if it was elsewhere)
                existingM.setMutantCycle(existingCycle);

                if (newM.getMutantResults() != null) {
                    existingM.setMutantResults(newM.getMutantResults());
                }
                toKeep.add(existingM);
            } else {
                newM.setMutantCycle(existingCycle);
                toKeep.add(newM);
            }
        }

        currentMutants.clear();
        currentMutants.addAll(toKeep);
    }

    private void saveMutantsQPrograms(List<Mutant> mutants) {
        if (mutants == null)
            return;
        for (Mutant mutant : mutants) {
            if (mutant.getQuirkCircuit() != null) {
                QProgram program = mutant.getQuirkCircuit();

                // Fix QCircuit reference
                prepareQProgram(program);

                // Save/Merge QProgram and UPDATE the mutant's reference
                QProgram savedProgram = qProgramRepository.save(program);
                mutant.setQuirkCircuit(savedProgram);
            }
            if (mutant.getOperator() != null && mutant.getOperator().getName() != null) {
                Optional<Operator> operator = operatorRepository.findByName(mutant.getOperator().getName());
                if (operator.isPresent()) {
                    mutant.setOperator(operator.get());
                }
            }
        }
    }

    // Helper method to ensure QCircuit is managed/saved correctly to avoid
    // duplicates
    private void prepareQProgram(QProgram program) {
        if (program == null || program.getQuirkCircuit() == null)
            return;

        QCircuit circuit = program.getQuirkCircuit();
        if (circuit.getId() != null) {
            Optional<QCircuit> existing = qCircuitRepository.findById(circuit.getId());
            if (existing.isPresent()) {
                program.setQuirkCircuit(existing.get());
            } else {
                program.setQuirkCircuit(qCircuitRepository.save(circuit));
            }
        } else {
            program.setQuirkCircuit(qCircuitRepository.save(circuit));
        }
    }

}
