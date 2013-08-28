package de.tum.os.models;

import de.tum.os.sa.shared.commands.Command;

/**
 * Created by Marius on 7/11/13.
 */
public interface ICommandExecuter {

    public void ExecuteCommand(Command command);
}
