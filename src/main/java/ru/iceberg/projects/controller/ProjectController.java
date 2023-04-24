package ru.iceberg.projects.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.iceberg.projects.service.ProjectService;

import javax.transaction.Transactional;

@RestController
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @Transactional
    public String addProject(@RequestParam("author") long id, @RequestParam("name") String name){
        return projectService.addProject(id, name);
    }

    @PostMapping("/long")
    @Transactional
    public String addProjectWithPath(@RequestParam("author") long id, @RequestParam("name") String name,
                             @RequestParam("path") String path){
        return projectService.addProject(id, name, path);
    }

    @GetMapping
    public String getProjectByTag(@RequestParam("tag") String tag){
        return projectService.findProjectsByTag(tag);
    }

    @GetMapping("/active")
    public String findActiveProjects(){
        return "Активные проекты: \n" + projectService.findProjectsByIsActive();
    }

    @GetMapping("/active/{id}")
    public String findMyActiveProjects(@PathVariable("id") long id) {
        return projectService.findMyActiveProjects(id);
    }

    @DeleteMapping("/{id}")
    public  String deleteProjectById(@PathVariable("id") long id){
        return projectService.deleteProjectById(id);
    }

    @PutMapping("/{id}")
    public String finishProjectById(@PathVariable("id") long id){
        return projectService.finishProjectById(id);
    }

    @GetMapping("/all")
    public String showAllProjects(){
        return projectService.showAllProjects();
    }

    @PostMapping("/{id}")
    public String changeProjectName(@PathVariable("id") long id, @RequestParam("name") String name){
        return projectService.changeName(id, name);
    }

    @PostMapping("/tags")
    public String addTag (@RequestParam("id") long id, @RequestParam("tag") String tag){
        return projectService.addTag(id, tag);
    }

    @GetMapping("/tags")
    public String findByTag(@RequestParam("tag") String tag){
        return projectService.findProjectsByTag(tag);
    }

    @PostMapping("/workers")
    public String addWorkerToProject(@RequestParam("projectid") long projectid, @RequestParam("workerid") int workerid){
        return projectService.addWorkerToProject(projectid, workerid);
    }

    @GetMapping("/report")
    public String reportMail(){
        return projectService.reportMail();
    }
}
