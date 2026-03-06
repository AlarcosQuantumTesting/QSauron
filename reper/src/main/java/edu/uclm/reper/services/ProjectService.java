package edu.uclm.reper.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.model.Mutant;
import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.MutantCycleId;
import edu.uclm.reper.model.Operator;
import edu.uclm.reper.model.Project;
import edu.uclm.reper.model.QCircuit;
import edu.uclm.reper.model.QProgram;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.OperatorRepository;
import edu.uclm.reper.repository.ProjectRepository;
import edu.uclm.reper.repository.QCircuitRepository;
import edu.uclm.reper.repository.QProgramRepository;
import edu.uclm.reper.repository.UserRepository;
import edu.uclm.reper.repository.MutantCycleRepository;
import jakarta.transaction.Transactional;

import edu.uclm.reper.dto.ProjectSummaryDTO;
import edu.uclm.reper.dto.MutantCycleSummaryDTO;
import edu.uclm.reper.dto.MutantSummaryDTO;
import edu.uclm.reper.dto.OperatorSummaryDTO;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QProgramRepository qProgramRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private QCircuitRepository qCircuitRepository;

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

        String userId = request.get("email");
        List<Project> projects = projectRepository.findProjectsByUserId(userId);
        for (Project project : projects) {
            project.getMutantCycles();
        }
        return projects;
    }

    // Obtiene version aligerada de proyectos para el sidebar
    public List<ProjectSummaryDTO> getSidebarProjectsByUser(String email) throws Exception {
        List<Project> projects = projectRepository.findProjectsByUserId(email);

        return projects.stream().map(project -> {
            List<MutantCycleSummaryDTO> cycleDTOs = new ArrayList<>();
            if (project.getMutantCycles() != null) {
                cycleDTOs = project.getMutantCycles().stream().map(cycle -> {
                    List<MutantSummaryDTO> mutantDTOs = new ArrayList<>();
                    if (cycle.getMutants() != null) {
                        mutantDTOs = cycle.getMutants().stream().map(mutant -> {
                            OperatorSummaryDTO opDto = null;
                            if (mutant.getOperator() != null) {
                                opDto = new OperatorSummaryDTO(mutant.getOperator().getName());
                            }
                            return new MutantSummaryDTO(mutant.getIndex(),
                                    opDto);
                        }).collect(Collectors.toList());
                    }
                    return new MutantCycleSummaryDTO(cycle.getId(), mutantDTOs);
                }).collect(Collectors.toList());
            }
            return new ProjectSummaryDTO(project.getId(), project.getName(), cycleDTOs);
        }).collect(Collectors.toList());
    }

    public List<edu.uclm.reper.dto.ProjectLightSummaryDTO> getLightSidebarProjectsByUser(String email)
            throws Exception {
        return projectRepository.findLightProjectsByUserId(email);
    }

    public List<Map<String, String>> getProjectsName(Map<String, String> request) throws Exception {

        List<Map<String, String>> projects = new ArrayList<Map<String, String>>();

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

    public edu.uclm.reper.dto.ProjectDetailsDTO getProjectDetailsDTO(Map<String, String> request) throws Exception {
        Project project = getProject(request);

        edu.uclm.reper.dto.ProjectDetailsDTO dto = new edu.uclm.reper.dto.ProjectDetailsDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());

        // QuCoDetails
        if (project.getQProgram() != null) {
            edu.uclm.reper.dto.QuCoDetailsDTO quCo = new edu.uclm.reper.dto.QuCoDetailsDTO();
            quCo.setQubits(project.getQProgram().getQubits());
            if (project.getQProgram().getGenerator() != null) {
                String genType = project.getQProgram().getGenerator().getClass().getSimpleName().toUpperCase();
                if (genType.contains("$HIBERNATEPROXY$")) {
                    genType = genType.substring(0, genType.indexOf("$"));
                }
                quCo.setGeneratorType(genType);
            }
            dto.setQuCoDetails(quCo);
        }

        // QuTeDetails
        List<edu.uclm.reper.dto.QuTeDetailsDTO> testDetails = new ArrayList<>();
        if (project.getTestSuites() != null) {
            for (edu.uclm.reper.model.TestSuite ts : project.getTestSuites()) {
                edu.uclm.reper.dto.QuTeDetailsDTO teDto = new edu.uclm.reper.dto.QuTeDetailsDTO();
                int count = ts.getTestCases() != null ? ts.getTestCases().size() : 0;
                teDto.setTestCasesCount(count);

                String sType = "Unknown";
                if (ts.getTestCases() != null && !ts.getTestCases().isEmpty()) {
                    edu.uclm.reper.model.TestCase firstTc = ts.getTestCases().get(0);
                    String className = firstTc.getClass().getSimpleName();
                    if (className.contains("$HibernateProxy$")) {
                        className = className.substring(0, className.indexOf("$"));
                    }
                    sType = className;
                }
                teDto.setSuiteType(sType);
                testDetails.add(teDto);
            }
        }
        dto.setQuTeDetails(testDetails);

        // QuMuDetails
        List<edu.uclm.reper.dto.QuMuDetailsDTO> muDetails = new ArrayList<>();
        if (project.getMutantCycles() != null) {
            for (edu.uclm.reper.model.MutantCycle mc : project.getMutantCycles()) {
                edu.uclm.reper.dto.QuMuDetailsDTO muDto = new edu.uclm.reper.dto.QuMuDetailsDTO();
                int count = mc.getMutants() != null ? mc.getMutants().size() : 0;
                muDto.setMutantsCount(count);
                muDetails.add(muDto);
            }
        }
        dto.setQuMuDetails(muDetails);

        // ProjectNotes
        List<edu.uclm.reper.dto.ProjectNoteDTO> noteDetails = new ArrayList<>();
        if (project.getNotes() != null) {
            for (edu.uclm.reper.model.ProjectNote n : project.getNotes()) {
                noteDetails.add(new edu.uclm.reper.dto.ProjectNoteDTO(
                        n.getId(), n.getTitle(), n.getText(), n.getType(), n.getTimestamp()));
            }
        }
        dto.setProjectNotes(noteDetails);

        return dto;
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
        if (project.getQProgram() != null) {
            prepareQProgram(project.getQProgram());
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
        if (incoming.getQProgram() != null) {
            QProgram incomingProgram = incoming.getQProgram();
            prepareQProgram(incomingProgram); // Fix QCircuit reference before attaching

            // Save/Merge QProgram explicitly and update reference
            // This ensures 'existing' refers to a managed QProgram, preventing duplicate
            // entry on cascade
            QProgram savedProgram = qProgramRepository.save(incomingProgram);
            existing.setQProgram(savedProgram);
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
                    Optional<MutantCycle> dbCycle = mutantCycleRepository
                            .findById(new MutantCycleId(existingProject.getId(), newMc.getId()));
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
            if (existingM != null) {
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

    // --- BATCH SAVING METHODS FOR OPTIMIZATION ---

    public void saveMutantsBatch(String projectId, int cycleId, List<Mutant> mutantsBatch) throws Exception {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new Exception("Project not found: " + projectId);
        }

        Optional<MutantCycle> cycleOpt = mutantCycleRepository.findById(new MutantCycleId(projectId, cycleId));
        if (cycleOpt.isEmpty()) {
            throw new Exception("MutantCycle not found: " + cycleId);
        }

        MutantCycle cycle = cycleOpt.get();

        // Prepare circuits and operators for the new batch
        saveMutantsQPrograms(mutantsBatch);

        List<Mutant> currentMutants = cycle.getMutants();
        if (currentMutants == null) {
            currentMutants = new ArrayList<>();
            cycle.setMutants(currentMutants);
        }

        Map<Integer, Mutant> currentMap = new HashMap<>();
        for (Mutant m : currentMutants) {
            currentMap.put(m.getId(), m);
        }

        for (Mutant newM : mutantsBatch) {
            Mutant existingM = null;

            if (newM.getId() != 0 && currentMap.containsKey(newM.getId())) {
                existingM = currentMap.get(newM.getId());
            }

            if (existingM != null) {
                // Update existing
                existingM.setMutantCycle(cycle);
                if (newM.getMutantResults() != null) {
                    for (var r : newM.getMutantResults())
                        r.setMutant(existingM);
                    if (existingM.getMutantResults() == null)
                        existingM.setMutantResults(new ArrayList<>());
                    else
                        existingM.getMutantResults().clear();
                    existingM.getMutantResults().addAll(newM.getMutantResults());
                }
                // No need to add to list, it's already there
            } else {
                // Add new
                newM.setMutantCycle(cycle);
                if (newM.getMutantResults() != null) {
                    for (var r : newM.getMutantResults())
                        r.setMutant(newM);
                }
                currentMutants.add(newM);
            }
        }

        mutantCycleRepository.save(cycle);
    }

    public void saveExecutionsBatch(String projectId, int cycleId, List<Mutant> mutantsWithResultsBatch)
            throws Exception {
        Optional<MutantCycle> cycleOpt = mutantCycleRepository.findById(new MutantCycleId(projectId, cycleId));
        if (cycleOpt.isEmpty()) {
            throw new Exception("MutantCycle not found: " + cycleId);
        }

        MutantCycle cycle = cycleOpt.get();
        List<Mutant> currentMutants = cycle.getMutants();
        if (currentMutants == null || currentMutants.isEmpty()) {
            return; // Nothing to update
        }

        // Create a fast lookup map for existing mutants by mutantIndex (or ID if
        // preferred, but frontend sends them with IDs hopefully, or index is unique per
        // cycle)
        Map<Integer, Mutant> currentMapByIndex = new HashMap<>();
        Map<Integer, Mutant> currentMapById = new HashMap<>();

        for (Mutant m : currentMutants) {
            currentMapById.put(m.getId(), m);
            currentMapByIndex.put(m.getIndex(), m);
        }

        for (Mutant batchMutant : mutantsWithResultsBatch) {
            Mutant target = null;
            if (batchMutant.getId() != 0 && currentMapById.containsKey(batchMutant.getId())) {
                target = currentMapById.get(batchMutant.getId());
            } else if (currentMapByIndex.containsKey(batchMutant.getIndex())) {
                target = currentMapByIndex.get(batchMutant.getIndex());
            }

            if (target != null && batchMutant.getMutantResults() != null) {
                // Ensure bidirectional relationship is maintained for newly added results
                for (var result : batchMutant.getMutantResults()) {
                    result.setMutant(target);
                }

                // Clear existing and add new to avoid duplicates, or just set it
                if (target.getMutantResults() == null) {
                    target.setMutantResults(new ArrayList<>());
                } else {
                    target.getMutantResults().clear();
                }
                target.getMutantResults().addAll(batchMutant.getMutantResults());
            }
        }

        mutantCycleRepository.save(cycle);
    }
}
