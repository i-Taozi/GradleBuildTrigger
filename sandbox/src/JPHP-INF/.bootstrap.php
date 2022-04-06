<?phpuse php\http\HttpServer;use php\http\HttpServerRequest;use php\http\HttpServerResponse;use php\io\Stream;use php\lang\System;use php\lib\fs;use php\lib\str;$server = new HttpServer(8080);$server->get("/", function (HttpServerRequest $request, HttpServerResponse $response) {    $response->write("<h1>Hello user!</h1><p>Upload files to me through the POST method</p>");});$server->post("/", function (HttpServerRequest $request, HttpServerResponse $response) {    foreach ($request->getParts() as $part) {        echo "Got new http part with name '{$part->getName()}'='{$part->getSubmittedFileName()}' and size '{$part->getSize()}'!\n";        $tempPath = generateTempPath();        Stream::putContents($tempPath, $part->readAll());        echo " -> saved as {$tempPath}\n";    }    $response->write("ok");});$server->run();/** * @return string */function generateTempPath(): string {    return fs::abs((System::getProperty("java.io.tempdir") ?: "/tmp") . "/" . str::uuid());}