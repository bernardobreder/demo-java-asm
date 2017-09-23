package asmj.emulador;

import javax.swing.JFrame;

import asmj.AsmCommand;
import asmj.emulador.x86.AbstractMachine;

public class AsmEnumadorFrame extends JFrame {

	/** Comandos */
	private AsmCommand[] cmds;
	/** Máquina */
	private AbstractMachine machine;
	
	public AsmEnumadorFrame(AsmCommand[] cmds,AbstractMachine machine) {
		this.cmds = cmds;
		this.machine = machine;
	}

}
