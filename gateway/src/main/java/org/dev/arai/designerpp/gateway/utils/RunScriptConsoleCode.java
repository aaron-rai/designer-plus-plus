package org.dev.arai.designerpp.gateway.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.common.script.JythonExecException;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class RunScriptConsoleCode {
    private static final Logger logger =
        LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".RunScriptConsoleCode");

    public PyCode convertToPyCode(String code) {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            return interpreter.compile(code, "<GatewayConsole>");
        }
    }
}