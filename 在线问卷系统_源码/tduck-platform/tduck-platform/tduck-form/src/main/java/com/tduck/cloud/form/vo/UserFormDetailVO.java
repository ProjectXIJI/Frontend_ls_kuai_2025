package com.tduck.cloud.form.vo;

import com.tduck.cloud.form.entity.UserFormEntity;
import com.tduck.cloud.form.entity.UserFormItemEntity;
import com.tduck.cloud.form.entity.UserFormLogicEntity;
import com.tduck.cloud.form.entity.UserFormThemeEntity;
import com.tduck.cloud.form.entity.enums.FormTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author smalljop
 */
@Data
@AllArgsConstructor
public class UserFormDetailVO {
    /**
     * 表单基础信息
     */
    private UserForm form;

    /**
     * 表单项
     */
    private List<UserFormItemEntity> formItems;

    /**
     * 主题
     */
    private UserFormThemeEntity userFormTheme;

    /**
     * 逻辑
     */
    private UserFormLogicEntity formLogic;

    @Data
    public static class UserForm {
        private String formKey;
        /**
         * 表单名称
         */
        private String name;
        /**
         * 表单描述
         */
        private String description;

        /**
         * 表单类型
         */
        private FormTypeEnum type;

        /**
         * 创建者用户ID
         */
        private Long userId;

        /**
         * 创建者用户名
         */
        private String userName;

        /**
         * 创建者用户名（用于前端显示）
         */
        private String createUserName;

        public UserForm(UserFormEntity entity) {
            this.formKey = entity.getFormKey();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.type = entity.getType();
            this.userId = entity.getUserId();
            this.userName = entity.getUserName();
            this.createUserName = entity.getCreateUserName();
        }

    }
}
