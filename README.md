## AWS Greengrass IP Detector

The IP detector component (aws.greengrass.clientdevices.IPDetector) does the following:
* Monitors the Greengrass core device's network connectivity information. This information includes the core device's network endpoints and the port where an MQTT broker operates.
* Updates the core device's connectivity information in the AWS IoT Greengrass cloud service.

Client devices can use Greengrass cloud discovery to retrieve associated core devices' connectivity information. Then, client devices can try to connect to each core device until they successfully connect.

The IP detector component replaces a core device's existing connectivity information with the information it detects. Because this component removes existing information, you can either use the IP detector component, or manually manage connectivity information.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the Apache 2.0 License. 
