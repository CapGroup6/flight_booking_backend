package fdu.capstone.system.module.controller;

import fdu.capstone.system.module.entity.UserPreferenceEntity;
import fdu.capstone.system.module.service.UserPreferenceService;
import fdu.capstone.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */


@Tag(name = "user preference controller")
@RestController
@RequestMapping("/userPreference")
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @PostMapping("/add")
    public ResponseResult add(@RequestBody UserPreferenceEntity userPreferenceEntity) {
        boolean success = userPreferenceService.saveUserPreference(userPreferenceEntity);
        if(success){
            return ResponseResult.success("add user preference success");
        }else {
            return ResponseResult.fail("add user preference fail");
        }
    }


    @GetMapping("/getByUserId")
    public ResponseResult add(@RequestParam Long userId) {
        List<UserPreferenceEntity>  userPreferenceEntityList = userPreferenceService.getUserPreferenceByUserId(userId);
        if(userPreferenceEntityList!=null && userPreferenceEntityList.size()>0){
            return ResponseResult.success(userPreferenceEntityList);
        }else {
            return ResponseResult.fail("the user do not have any preference");
        }
    }





}
