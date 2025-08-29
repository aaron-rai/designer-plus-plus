package org.dev.arai.designerpp.gateway.utils;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.common.script.JythonExecException;
import com.inductiveautomation.ignition.common.script.ScriptFunction;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.ScriptManager.UndefinedFunctionException;

public class RunScriptConsoleCode {
    private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".RunScriptConsoleCode");

    public void runCode(ScriptManager scriptManager, String code) throws PyException, JythonExecException, UndefinedFunctionException {
        logger.info("Running script code: {}", code);

        ScriptFunction func = scriptManager.compileFunction(code);
		scriptManager.runFunction(func, null);
    }
}
