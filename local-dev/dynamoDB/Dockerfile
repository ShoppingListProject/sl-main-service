FROM amazon/dynamodb-local:3.1.0

# Swich to root
USER root

# Install AWS CLI for interacting with DynamoDB
RUN yum install -y curl-minimal unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws

# Copy initialization files
WORKDIR /dynamodb
COPY table.json items.json setup.sh ./
RUN chmod +x setup.sh

# Entrypoint script will handle both DynamoDB startup and initialization
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8000
ENTRYPOINT ["/entrypoint.sh"]

