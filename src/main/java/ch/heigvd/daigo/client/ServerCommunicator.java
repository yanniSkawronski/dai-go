package ch.heigvd.daigo.client;

import ch.heigvd.daigo.server.ServerReply;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

class ClientInput {
    ClientRequest request;
    String[] args;

    ClientInput(ClientRequest request, Object...args) {
        this.request = request;
        this.args = new String[args.length];
        for(int i = 0; i<args.length; ++i)
            this.args[i] = args[i].toString();
    }
}

class ServerOutput {
    ServerReply reply = null;
    String[] args = null;

    ServerOutput(ServerReply reply, Object...args) {
        this.reply = reply;
        this.args = new String[args.length];
        for(int i = 0; i<args.length; ++i)
            this.args[i] = args[i].toString();
    }
}

class ServerCommunicator {
    BufferedReader in;
    BufferedWriter out;

    ServerCommunicator(BufferedReader in, BufferedWriter out) {
        this.in = in; this.out = out;
    }

    ServerOutput send(ClientInput input) throws IOException {
        StringBuilder sb = new StringBuilder(input.request.toString());
        for(String arg : input.args)
            sb.append(" ").append(arg);
        out.write(sb.toString()+"\n");
        out.flush();
        String response = in.readLine();
        if(response==null)
            throw new RuntimeException("Server has unexpectedly disconnected.");
        String[] responses = response.split(" ");

        ServerReply reply = ServerReply.valueOf(responses[0]);
        Object[] args = new String[responses.length - 1];
        for(int i = 1; i<responses.length; ++i)
            args[i-1] = responses[i];

        return new ServerOutput(reply, args);
    }
    ServerOutput send(ClientRequest request, Object...args) throws IOException {
        ClientInput input = new ClientInput(request, args);
        return send(input);
    }
}
