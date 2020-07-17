package com.aptech.project.project_sem4.controller;

import com.aptech.project.project_sem4.model.*;
import com.aptech.project.project_sem4.model.Class;
import com.aptech.project.project_sem4.repository.UserRepository;
import com.aptech.project.project_sem4.service.QuizService;
import com.aptech.project.project_sem4.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private QuizService quizService;
@Autowired
    private UserRepository userRepository;
    private Random random = new Random();

    @RequestMapping(value = {"/", "/section"}, method = RequestMethod.GET)
    public String section(Model model) {
        List<User> listUser = userService.listAll();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        ObjectId user_id = userService.findByEmail(userName).getId();
        List<Section> listSection = userService.listAllSection();
        List<Result> listSection_Result = userService.getListSection_Result(user_id.toString());
        for(int i = 0; i< listSection.size(); i++)
        {
            for(int j = 0; j < listSection_Result.size(); j++)
            {
                if(listSection.get(i).getId().equals(listSection_Result.get(j).getSection_id()))
                {
                    listSection.remove(i);
                }
            }
        }
        model.addAttribute("listUser", listUser);
        model.addAttribute("listSection", listSection);
        return "section";
    }


    @RequestMapping(value = {"/changeUserProfile"}, method = RequestMethod.POST)
    public String changeUserProfile(Model model, HttpServletRequest request)
    {
        BCryptPasswordEncoder bCryptPasswordEncoder  = new BCryptPasswordEncoder();
        String lastName = request.getParameter("lastName");
        String firstName = request.getParameter("firstName");
        String userEmail = request.getParameter("email");
        String userPass = request.getParameter("password");
        String re_pass = request.getParameter("re_password");
        if(userPass.equals(re_pass)) {
            String pass = bCryptPasswordEncoder.encode(userPass);
            System.out.println(pass);
            User new_user = userService.findByEmail(userEmail);
            new_user.setFirstName(firstName);
            new_user.setLastName(lastName);
            new_user.setPassword(pass);
            userService.saveUser(new_user);
            return "Yes";
        }
        else {
            return "NO";
        }
    }

    @RequestMapping(value = {"/userResult"})
    public String userResult(Model model)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User current_user = userService.findByEmail(userName);
        List<Result> listResult = userService.getListSection_Result(current_user.getId().toString());
        List<Section> listSection = userService.listAllSection();
        model.addAttribute("listResult", listResult);
        model.addAttribute("listSection", listSection);
        return "userResult";
    }

    @RequestMapping(value = {"/createQuiz"})
    public String createQuiz(Model model, HttpServletRequest request) {
        String params = request.getQueryString();
        String[] section_id = params.split("=");
        System.out.println(section_id[1]);
        List<Topic> listTopic = quizService.listAllTopicBySection_id(section_id[1]);
        // model.addAttribute("listTopic", listTopic);
        System.out.println(section_id[1]);
        List<Question> listQuestion = quizService.listAllQuestion();
        List<Question> listQuestionyByTopic = new ArrayList<Question>();

        UserController obj = new UserController();
        List<Question> newListQuestion = new ArrayList<Question>();

        for (int i = 0; i < listTopic.size(); i++) {
            listQuestionyByTopic.addAll(quizService.listQuestionByTopic(listTopic.get(i).getId().toString()));
        }

        for (int i = 0; i < 10; i++) {
            Question a = getRandomList(listQuestionyByTopic);
            newListQuestion.add(a);
            listQuestionyByTopic.remove(a);
        }
        model.addAttribute("listQuestion", newListQuestion);
        List<Choice> listChoice = new ArrayList<Choice>();
        for (int i = 0; i < 10; i++) {
            String question_id = newListQuestion.get(i).getId().toString();
            listChoice.addAll(quizService.listChoiceByQuestion_id(question_id));
            System.out.println(question_id);
        }
        for (int i = 0; i < 10; i++) {
            Session session_question = new Session();
            session_question.setQuestion_id(newListQuestion.get(i).getId());
            ObjectId choice_Pre = new ObjectId();
            session_question.setChoice_id(choice_Pre);
            ObjectId sectionId = new ObjectId(section_id[1]);
            session_question.setSection_id(sectionId);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ObjectId user_id = userService.findByEmail(userName).getId();
            session_question.setUser_id(user_id);
            quizService.saveSession(session_question);
        }
        model.addAttribute("listChoice", listChoice);
        model.addAttribute("section", section_id[1]);
        return "quiz";
    }

    public Question getRandomList(List<Question> list) {
        //0-4
        int index = random.nextInt(list.size());
        System.out.println("\nIndex :" + index);
        return list.get(index);
    }

}

