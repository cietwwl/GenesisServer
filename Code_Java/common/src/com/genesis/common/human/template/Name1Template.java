package com.genesis.common.human.template;

import com.genesis.core.template.annotation.ExcelRowBinding;
import com.genesis.core.template.exception.TemplateConfigException;

@ExcelRowBinding
public class Name1Template extends Name1TemplateVO {

    @Override
    public void check() throws TemplateConfigException {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return this.name;
    }

}
