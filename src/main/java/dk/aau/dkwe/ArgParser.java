package dk.aau.dkwe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Parser and container of program arguments
 */
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
        OUTPUT("-output"),
        CONFIG("-config"),
        TYPE("-type");

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
                            OUTPUT.toString().equals(str) ? OUTPUT :
                                    CONFIG.toString().equals(str) ? CONFIG :
                                            TYPE.toString().equals(str) ? TYPE : null;
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
        if (this.command == Command.INDEX)
        {
            checkIndexing();
        }

        else
        {
            checkLinking();
        }
    }

    private void checkIndexing()
    {
        if (this.parameters.size() != 2)
        {
            this.parsed = false;
            this.parseError = "Expects two parameters when indexing";
        }

        Iterator<Parameter> parameters = this.parameters.iterator();
        boolean hasDir = false, hasConfig = false;

        while (parameters.hasNext())
        {
            Parameter parameter = parameters.next();

            if (parameter == Parameter.DIRECTORY)
            {
                hasDir = true;
            }

            else if (parameter == Parameter.CONFIG)
            {
                hasConfig = true;
            }

            if (parameter.getValue() == null)
            {
                this.parsed = false;
                this.parseError = "Missing value for '" + parameter + "' parameter";
                return;
            }
        }

        if (!(hasDir && hasConfig))
        {
            this.parsed = false;
            this.parseError = "Indexing requires parameters '-dir' and '-config'";
        }
    }

    private void checkLinking()
    {
        if (this.parameters.size() != 5)
        {
            this.parsed = false;
            this.parseError = "Expects five parameters when linking";
        }

        Iterator<Parameter> params = this.parameters.iterator();
        boolean hasDir = false, hasOutput = false, hasTable = false, hasConfig = false, hasType = false;

        while (params.hasNext())
        {
            Parameter p = params.next();

            switch (p)
            {
                case DIRECTORY -> hasDir = true;
                case TABLE -> hasTable = true;
                case OUTPUT -> hasOutput = true;
                case CONFIG -> hasConfig = true;
                case TYPE -> hasType = true;
            }

            if (p.getValue() == null)
            {
                this.parsed = false;
                this.parseError = "Missing value for '" + p + "' parameter";
                return;
            }
        }

        if (!(hasDir && hasOutput && hasTable && hasConfig && hasType))
        {
            this.parsed = false;
            this.parseError = "Linking requires parameters '-dir', '-table', '-output', '-config', and '-type'";
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
