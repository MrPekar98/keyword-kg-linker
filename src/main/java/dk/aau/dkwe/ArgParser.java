package dk.aau.dkwe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ArgParser
{
    public enum Command
    {
        INDEX("index"),
        LINK("link");

        Command(String command)
        {
            this.command = command;
        }

        private final String command;

        @Override
        public String toString()
        {
            return this.command;
        }

        public static Command parse(String str)
        {
            return INDEX.toString().equals(str) ? INDEX :
                    LINK.toString().equals(str) ? LINK : null;
        }
    }

    public enum Parameter
    {
        DIRECTORY("-dir"),
        TABLE("-table"),
        OUTPUT("-output");

        Parameter(String parameter)
        {
            this.param = parameter;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        private String param;
        private String value = null;

        @Override
        public String toString()
        {
            return this.param;
        }

        public String getValue()
        {
            return this.value;
        }

        public static Parameter parse(String str)
        {
            return DIRECTORY.toString().equals(str) ? DIRECTORY :
                    TABLE.toString().equals(str) ? TABLE :
                            OUTPUT.toString().equals(str) ? OUTPUT : null;
        }
    }

    private boolean parsed = true;
    private String parseError = "";
    private final Command command;
    private final Set<Parameter> parameters = new HashSet<>();

    public static ArgParser parse(String[] args)
    {
        return new ArgParser(args);
    }

    private ArgParser(String[] args)
    {
        this.command = Command.parse(args[0]);

        if (this.command == null)
        {
            this.parsed = false;
            this.parseError = "'" + args[1] + "' not recognized";
        }

        for (int i = 1; i < args.length; i++)
        {
            Parameter p = Parameter.parse(args[i]);

            if (p == null)
            {
                this.parsed = false;
                this.parseError = "Could not parse parameter '" + args[i] + "'";
                break;
            }

            p.setValue(args[++i]);
            this.parameters.add(p);
        }

        if (this.parsed)
        {
            check();
        }
    }

    private void check()
    {
        if (this.command == Command.LINK)
        {
            if (this.parameters.size() != 1)
            {
                this.parsed = false;
                this.parseError = "Expects one parameter when indexing";
            }

            Parameter p = this.parameters.iterator().next();

            if (p != Parameter.DIRECTORY)
            {
                this.parsed = false;
                this.parseError = "Expects a directory to store index files";
            }

            else if (p.getValue() == null)
            {
                this.parsed = false;
                this.parseError = "Missing directory for '-dir' parameter";
            }
        }

        else    // Otherwise, it's guaranteed to be Command.INDEX
        {
            if (this.parameters.size() != 3)
            {
                this.parsed = false;
                this.parseError = "Expects three parameters when indexing";
            }

            Iterator<Parameter> params = this.parameters.iterator();
            boolean hasDir = false, hasOutput = false, hasTable = false;

            while (params.hasNext())
            {
                Parameter p = params.next();

                if (p == Parameter.DIRECTORY)
                {
                    hasDir = true;
                }

                else if (p == Parameter.TABLE)
                {
                    hasTable = true;
                }

                else if (p == Parameter.OUTPUT)
                {
                    hasOutput = true;
                }

                if (p.getValue() == null)
                {
                    this.parsed = false;
                    this.parseError = "Missing value for '" + p + "' parameter";
                    return;
                }
            }

            if (!(hasDir && hasOutput && hasTable))
            {
                this.parsed = false;
                this.parseError = "Indexing requires parameters '-dir', '-table', and '-output'";
            }
        }
    }

    public boolean isParsed()
    {
        return this.parsed;
    }

    public String parseError()
    {
        return this.parseError;
    }

    public Command getCommand()
    {
        return this.command;
    }

    public Set<Parameter> getParameters()
    {
        return this.parameters;
    }
}
