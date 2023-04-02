## Confetti API documentation

Confetti uses a [GraphQL](https://en.wikipedia.org/wiki/GraphQL) API. The GraphQL endpoint is available at:

```
https://confetti-app.dev/graphql
```

You can explore the API using the [sandbox]:

```
https://confetti-app.dev/sandbox
```

![sandbox](https://user-images.githubusercontent.com/3974977/229364452-21c8e97e-fed1-47b0-8a41-d679884e579d.png)

For an example, the following query (used in the screenshot above) returns the 100 first sessions:

```graphql
query {
  sessions(first: 100) {
    nodes {
      id
      title
      speakers {
        name
      }
    }
  }
}
```


### Choosing your conference

The API uses the value of the `"conference"` HTTP header to return data for a specific conference:

![Headers](https://user-images.githubusercontent.com/3974977/229364454-40695db2-d25e-41aa-bdda-49bcd8e50f63.png)

To get a list of available conferences at a given point in time, use the root `conferences` field:

```graphql
query {
  conferences {
    id
    name
  }
}
```

### Authentication

Setting and reading bookmarks requires to be authenticated. Authentication is handled through [Firebase authentication](https://firebase.google.com/docs/auth). Retrieve an id token with `Firebase.auth.currentUser?.getIdToken()` and pass it in your "Authorization" headers:

```
Authorization: Bearer ${firebaseIdToken}
```


