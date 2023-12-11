const toggleSidebar = () => {
  if ($ ('.sidebar').is (':visible')) {
    $ ('.sidebar').css ('display', 'none');
    $ ('.content').css ('margin-left', '0%');
  } else {
    $ ('.sidebar').css ('display', 'block');
    $ ('.content').css ('margin-left', '18%');
  }
};

const search = async () => {
  //console.log("searching....");

  let query = $ ('#search-input').val ();

  if (query == '') {
    $ ('.search-result').hide ();
    $ ('.table').css ('margin-top', '0%');
  } else {
    //search
    console.log (query);

    //sending request to the server
    let url = `http://localhost:8080/search/${query}`;

    try {
      const response = await fetch (url);

      if (!response.ok) {
        throw new Error (`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json ();

      //data.......
      console.log (data);

      let text = `<div class='list-group'>`;

      data.forEach (contact => {
        text =
          text+= `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-action'> ${contact.name} </a>`;
      });

      text += `</div>`;

      $ ('.search-result').html (text);
      $ ('.search-result').show ();
      $ ('.table').css ('margin-top', '10%');
    } catch (error) {
      console.error ('Error fetching data:', error.message);
    }
  }
};
